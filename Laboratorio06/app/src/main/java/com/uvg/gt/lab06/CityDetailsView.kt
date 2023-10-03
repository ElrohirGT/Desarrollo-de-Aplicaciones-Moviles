package com.uvg.gt.lab06

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

private const val LOG_TAG = "CityDetailsView - "

@Composable
fun CityDetailsView(navController: NavHostController, cityId: String, cityName: String) {
    val defaultImagesState: List<String>? = null
    val (images, setImages) = remember { mutableStateOf(defaultImagesState) }
    val request =
        Request.Builder().url("https://api.teleport.org/api/urban_areas/slug:${cityId}/images")
            .build()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(cityName, style = MaterialTheme.typography.displayLarge)
        if (images == null) {
            HTTP_CLIENT.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(LOG_TAG, "Response failed with error: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    val localTag = LOG_TAG + "OnResponse"
                    response.use {
                        if (!response.isSuccessful) {
                            Log.e(localTag, "Response was not successfull!")
                            return
                        }

                        Log.d(localTag, "Response was successfull!")

                        val responseBody = response.body!!.string()
                        val photos = JSONObject(responseBody).getJSONArray("photos")

                        Log.d(
                            localTag,
                            "JSON photos array obtained! Found (${photos.length()}) photos..."
                        )

                        val parsedImages = generateSequence(0) { it + 1 }.take(photos.length())
                            .map {
                                photos.getJSONObject(it).getJSONObject("image").getString("mobile")
                            }
                            .toList()

                        Log.d(localTag, "JSON photos parsed!")
                        setImages(parsedImages)
                    }
                }
            })
            Image(painter = painterResource(id = R.drawable.placeholder_image), contentDescription = "Loading city image")
        } else {
            LazyColumn {
                images.forEach {
                    item {
                        AsyncImage(
                            model = it,
                            contentDescription = "City image",
                            placeholder = painterResource(id = R.drawable.placeholder_image),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        Button(onClick = { navController.popBackStack() }) {
            Text("Go back")
        }
    }
}