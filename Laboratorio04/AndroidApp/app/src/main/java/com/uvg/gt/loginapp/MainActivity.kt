package com.uvg.gt.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uvg.gt.loginapp.ui.theme.LoginAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainComponent()
                }
            }
        }
    }
}

@Composable
fun MainComponent() {
    val navController = rememberNavController();

    NavHost(navController = navController, startDestination = "Main") {
        composable("Main") { MainView(navController) }
        composable("Register") { RegisterView(navController) }
        composable("Login") { LoginView(navController) }
        composable("Home") { HomeView(navController) }
    }
}

@Composable
fun MainView(navController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
        Text("Bienvenido!", style = MaterialTheme.typography.displayLarge)
        Button(onClick = { navController.navigate("Login") }) {
            Text("Login")
        }
        TextButton(onClick = { navController.navigate("Register") }) {
            Text("Register")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewPreview() {
    LoginAppTheme {
        MainView(rememberNavController())
    }
}