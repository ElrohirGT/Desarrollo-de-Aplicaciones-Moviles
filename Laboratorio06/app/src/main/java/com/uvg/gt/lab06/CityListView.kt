package com.uvg.gt.lab06

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.uvg.gt.lab06.ui.theme.Lab06Theme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class City(val name: String, val id: String)

private val LOG_TAG = "CityListView - "

@Composable
fun CityListView(navController: NavHostController) {
    val request = Request.Builder().url("https://api.teleport.org/api/urban_areas/").build()
    val (cityList, setCityList) = remember { mutableStateOf(listOf<City>()) }
    val (isLoading, setIsLoading) = remember { mutableStateOf(true) }

    Log.d(LOG_TAG + "Main", "Calling API for cities...")

    if (isLoading) {
        HTTP_CLIENT.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(LOG_TAG + "OnFailure", "Failed to get cities!")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(LOG_TAG + "OnResponse", "Request of cities was not successful!")
                    }

                    Log.d(LOG_TAG + "OnResponse", "Request was successful!")

                    val jsonBody = response.body!!.string()
                    val cities =
                        JSONObject(jsonBody).getJSONObject("_links").getJSONArray("ua:item")

                    Log.d(LOG_TAG + "OnResponse", "Cities obtained from response!")

                    val parsedCities =
                        generateSequence(0) { it + 1 }.take(cities.length()).map {
                            val obj = cities.getJSONObject(it)
                            City(
                                obj.getString("name"),
                                obj.getString("href").split(":")[2].dropLast(1)
                            )
                        }.toList()

                    Log.d(LOG_TAG + "OnResponse", "Cities parsed!")

                    setCityList(parsedCities)
                    setIsLoading(false)
                    Log.d(LOG_TAG + "OnResponse", "Obtained all the cities!")
                }
            }
        })
    }

    if (isLoading) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Loading cities...")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            cityList.forEach {
                item {
                    TextButton(onClick = {
                        Log.d(LOG_TAG + "City Click", "Clicked city with id ${it.id}")
                        Log.d(LOG_TAG + "City Click", "Navigating to ${it.id}...")
                        navController.navigate("CityDetails/${it.id}/${it.name}")
                    }) {
                        Text(it.name)
                    }
                }
            }
        }
    }

}

@Composable
fun CityListViewPreview() {
    Lab06Theme {
        CityListView(rememberNavController())
    }
}