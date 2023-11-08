@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)

package com.uvg.gt.laboratorio12

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uvg.gt.laboratorio12.ui.theme.Laboratorio12Theme
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Laboratorio12Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val windowSizeClass = calculateWindowSizeClass(this)
                    MainComponent(windowSizeClass)
                }
            }
        }
    }
}

@Composable
fun MainComponent(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    val (input, setInput) = remember { mutableStateOf("") }
    val images = arrayOf(
        R.drawable.meme00,
        R.drawable.meme01,
        R.drawable.meme02,
        R.drawable.meme03,
        R.drawable.meme04,
        R.drawable.meme05,
        R.drawable.meme06,
        R.drawable.meme07,
        R.drawable.meme08,
        R.drawable.meme09,
        R.drawable.meme10,
        R.drawable.meme11,
        R.drawable.meme12,
        R.drawable.meme13,
        R.drawable.meme14,
        R.drawable.meme15,
        R.drawable.meme16,
        R.drawable.meme17,
        R.drawable.meme18,
        R.drawable.meme19,
        R.drawable.meme20,
        R.drawable.meme21,
        R.drawable.meme22,
        R.drawable.meme23,
        R.drawable.meme24,
        R.drawable.meme25,
        R.drawable.meme26,
        R.drawable.meme27,
        R.drawable.meme28,
        R.drawable.meme29,
        R.drawable.meme30,
        R.drawable.meme31,
        R.drawable.meme32,
        R.drawable.meme33,
        R.drawable.meme34,
        R.drawable.meme35,
        R.drawable.meme36,
        R.drawable.meme37,
        R.drawable.meme38,
        R.drawable.meme39,
        R.drawable.meme40,
        R.drawable.meme41,
        R.drawable.meme42,
        R.drawable.meme43,
        R.drawable.meme44,
        R.drawable.meme45,
        R.drawable.meme46,
        R.drawable.meme47,
        R.drawable.meme48,
        R.drawable.meme49,
        R.drawable.meme50,
        R.drawable.meme51,
        R.drawable.meme52,
        R.drawable.meme53,
        R.drawable.meme54,
        R.drawable.meme55,
        R.drawable.meme56,
    )

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido!", style = MaterialTheme.typography.displayLarge)
            TextField(value = input, onValueChange = setInput, modifier.fillMaxWidth())
            Button(onClick = { /*TODO*/ }, modifier = modifier.fillMaxWidth()) {
                Text("¡Subir!")
            }

            Spacer(modifier = modifier.height(10.dp))
            Text("Lista...", style = MaterialTheme.typography.displaySmall)
            LazyColumn(contentPadding = PaddingValues(10.dp)) {
                items(images) {
                    Image(painter = painterResource(id = it), contentDescription = "Meme image", modifier = modifier
                        .fillMaxWidth(), contentScale = ContentScale.FillWidth)
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido!", style = MaterialTheme.typography.displayLarge)
            Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                TextField(value = input, onValueChange = setInput)
                Button(onClick = { /*TODO*/ }) {
                    Text("¡Subir!")
                }
            }

            Spacer(modifier = modifier.height(10.dp))
            Text("Lista...", style = MaterialTheme.typography.displaySmall)

            val columns =  if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium) {
                5
            } else {
                10
            }
            LazyVerticalGrid(columns = GridCells.Fixed(columns), contentPadding = PaddingValues(10.dp)) {
                items(images.size) {
                    Image(painter = painterResource(id = images[it]), contentDescription = "Meme image", modifier = modifier
                        .fillMaxWidth()
                        .padding(10.dp), contentScale = ContentScale.FillWidth)
                }
            }
        }
    }
}
