package com.uvg.gt.lab06

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uvg.gt.lab06.ui.theme.Lab06Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab06Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}

@Composable
fun MainView() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "CityList") {
        composable("CityList") { CityListView(navController) }
        composable(
            "CityDetails/{cityId}/{cityName}",
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType },
                navArgument("cityName") { type = NavType.StringType })
        ) { backStackEntry ->
            CityDetailsView(
                navController,
                backStackEntry.arguments?.getString("cityId")!!,
                        backStackEntry.arguments?.getString("cityName")!!
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Lab06Theme {
        MainView()
    }
}