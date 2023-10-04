package com.uvg.gt.lab05

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.uvg.gt.lab05.ui.theme.Lab05Theme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

data class AnimeInfo(
    var title_english: String,
    val title_japanese: String,
    val type: String,
    var imageUrl: String,
    val episodes: Int,
    val mal_id: Int,
)

private const val LOG_TAG = "HomeView - "

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val request = Request.Builder().url("https://api.jikan.moe/v4/random/anime").build()
    val defaultAnime: AnimeInfo? = null
    val (anime, setAnime) = remember { mutableStateOf(defaultAnime) }

    val getNewAnime = { setAnime(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Anime Compendium",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = getNewAnime) {
            Text("Load Another!")
        }

        if (anime == null) {
            apiGetAnime(request, setAnime)
            Text("Loading...")
        } else {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = {
                    navController.navigate("AnimeDetailView/${anime.mal_id}")
                }
            ) {
                Text(
                    anime.title_english,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )
                AsyncImage(
                    model = anime.imageUrl,
                    contentDescription = "Anime ${anime.title_english} banner",
                    placeholder = painterResource(
                        id = R.drawable.placeholder_image
                    ),
                    modifier = modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    "${anime.type} - ${anime.episodes}",
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )
                Text(
                    anime.title_japanese,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )
            }
        }

    }

}

@Preview
@Composable
fun HomeViewPreview() {
    Lab05Theme {
        HomeView()
    }
}

fun apiGetAnime(
    request: Request,
    onGetCompletes: (AnimeInfo) -> Unit,
    onGetFails: (IOException) -> Unit = {}
) {
    CLIENT.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: java.io.IOException) {
            Log.e(LOG_TAG + "OnFailure", "Error Getting anime: $e")
            onGetFails(e)
        }

        override fun onResponse(call: Call, response: Response) {
            val localTag = LOG_TAG + "OnResponse"
            response.use {
                if (!response.isSuccessful) {
                    Log.e(localTag, "Response completed but failed!")
                    return
                }

                Log.d(localTag, "Response completed successfully!")
                val jsonBody = response.body!!.string()
                val jsonObj = JSONObject(jsonBody).getJSONObject("data")

                Log.d(localTag, "Data: $jsonObj")
                val parsedBody = GSON.fromJson(
                    jsonObj.toString(),
                    AnimeInfo::class.java
                )

                if (parsedBody.title_english == null) {
                    parsedBody.title_english = jsonObj.getString("title")
                }

                if (parsedBody.imageUrl == null) {
                    parsedBody.imageUrl =
                        jsonObj.getJSONObject("images").getJSONObject("jpg")
                            .getString("large_image_url")
                }

                Log.d(localTag, "Parsed JSON to AnimeInfo: $parsedBody")
                onGetCompletes(parsedBody)
            }
        }
    })
}