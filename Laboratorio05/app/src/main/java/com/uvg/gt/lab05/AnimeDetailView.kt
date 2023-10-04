package com.uvg.gt.lab05

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class AnimeEpisode(
    val url: String,
    val title: String,
    val filler: Boolean,
)

data class PaginationInfo(val last_visible_page: Int, val has_next_page: Boolean)

private const val LOG_TAG = "AnimeDetailView - "

@Composable
fun AnimeDetailView(
    animeId: Int,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val (pagingInfo, setPagingInfo) = remember {
        mutableStateOf(
            PaginationInfo(
                1,
                false
            )
        )
    }
    val request =
        Request.Builder()
            .url("https://api.jikan.moe/v4/anime/${animeId}/episodes?page=${pagingInfo.last_visible_page}")
            .build()
    val (episodes, setEpisodes) = remember { mutableStateOf(emptyList<AnimeEpisode>()) }
    val (isLoading, setIsLoading) = remember { mutableStateOf(true) }

    val nextPage = {
        setIsLoading(true)
        setPagingInfo(PaginationInfo(pagingInfo.last_visible_page + 1, false))
    }
    val previousPage = {
        setIsLoading(true)
        setPagingInfo(PaginationInfo(pagingInfo.last_visible_page - 1, false))
    }
    val hasPreviousPage = pagingInfo.last_visible_page > 1

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back!")
        }

        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = previousPage, enabled = hasPreviousPage) {
                Text("<")
            }

            Text(pagingInfo.last_visible_page.toString())

            Button(onClick = nextPage, enabled = pagingInfo.has_next_page) {
                Text(">")
            }
        }

        if (isLoading) {
            apiGetEpisodes(request, {
                setEpisodes(it)
                setIsLoading(false)
            }, setPagingInfo)
            Text("Loading...")
        } else {
            LazyColumn {
                episodes.forEach {
                    item {
                        TextButton(
                            onClick = { /*TODO Possibly add navigation to episode*/ },
                            shape = RectangleShape
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(8.dp, 0.dp)
                            ) {
                                Text(it.title)
                                if (it.filler) {
                                    Icon(
                                        painter = painterResource(R.drawable.visibility_off),
                                        contentDescription = "Filler episode icon"
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.visibility_on),
                                        contentDescription = "Not filler episode icon"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (episodes.isEmpty()) {
                Text("The API couldn't load the episodes!", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun apiGetEpisodes(
    request: Request,
    onResponseSuccess: (List<AnimeEpisode>) -> Unit,
    onExtra: (PaginationInfo) -> Unit
) {
    CLIENT.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(LOG_TAG + "OnFailure", "An error occurred: $e")
        }

        override fun onResponse(call: Call, response: Response) {
            val localTag = LOG_TAG + "OnResponse"
            response.use {
                if (!response.isSuccessful) {
                    Log.e(localTag, "Response was not successfull!")
                    Log.e(localTag, response.message)
                    Log.e(localTag, response.body!!.string())
                    return
                }

                Log.d(localTag, "Response completed successfully")
                val jsonBody = response.body!!.string()
                val dataArray = JSONObject(jsonBody).getJSONArray("data")
                val paginationObject = JSONObject(jsonBody).getJSONObject("pagination")

                Log.d(localTag, "Body retrieved without errors")

                val episodeList = generateSequence(0) { it + 1 }.take(dataArray.length()).map {
                    GSON.fromJson(dataArray.get(it).toString(), AnimeEpisode::class.java)
                }.toList()
                val paginationInfo =
                    GSON.fromJson(paginationObject.toString(), PaginationInfo::class.java)

                Log.d(localTag, "Parsed episode list: $episodeList")
                Log.d(localTag, "Parsed pagination info: $paginationInfo")

                onResponseSuccess(episodeList)
                onExtra(paginationInfo)
            }
        }

    })
}
