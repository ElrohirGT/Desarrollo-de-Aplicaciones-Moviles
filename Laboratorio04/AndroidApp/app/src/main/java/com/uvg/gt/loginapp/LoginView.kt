package com.uvg.gt.loginapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.uvg.gt.loginapp.ui.theme.LoginAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(navHostController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
        Text("Login", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(15.dp))

        val (email, setEmail) = remember { mutableStateOf("") }
        val (password, setPassword) = remember { mutableStateOf("") }

        Text("E-mail:")
        TextField(value = email, onValueChange = { setEmail(it) })
        Text("Password:")
        TextField(value = password, onValueChange = { setPassword(it) })

        Spacer(modifier = Modifier.height(15.dp))
        Row {
            ElevatedButton(onClick = { navHostController.popBackStack() }) {
                Text("Go Back")
            }
            Button(onClick = { /*TODO*/ }) {
                Text("Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    LoginAppTheme {
        LoginView(rememberNavController())
    }
}