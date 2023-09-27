package com.uvg.gt.loginapp

import android.app.AlertDialog
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.uvg.gt.loginapp.ui.theme.LoginAppTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(navHostController: NavHostController, onJwtRetrieved: (String) -> Unit) {
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    val defaultErrorMessage: String? = null
    val (errorMessage, setErrorMessage) = remember { mutableStateOf(defaultErrorMessage) }
    val context = LocalContext.current

    if (errorMessage != null) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error!")
        builder.setMessage(errorMessage)
        builder.setPositiveButton("Ok") { _, _ -> setErrorMessage(null) }
        builder.create()
    }

    val defaultJwt: String? = null
    val (JWT, setJWT) = remember { mutableStateOf(defaultJwt) }
    LaunchedEffect(JWT) {
        Log.d("LoginView - JWT", "LaunchedEffect started...")

        if (JWT != null) {
            Log.d("LoginView - JWT", "JWT saved: $JWT")
            navHostController.navigate("Home")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ) {
        Text("Login", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(15.dp))

        Text("E-mail:")
        TextField(value = email, onValueChange = { setEmail(it) })
        Text("Password:")
        TextField(
            value = password,
            onValueChange = { setPassword(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(15.dp))
        Row {
            ElevatedButton(onClick = { navHostController.popBackStack() }) {
                Text("Go Back")
            }
            Button(onClick = {
                Log.d("LoginView - OnClick", "Creando request...")
                val request = Request.Builder()
                    .url("$HOST/login")
                    .post(
                        GSON.toJson(LoginPayload(email, password))
                            .toRequestBody(MEDIA_TYPE_JSON)
                    )
                    .build()

                Log.d("LoginView - OnClick", "Enviando request...")
                HTTP_CLIENT.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("LoginView - OnFailure", "Ocurrió un error!")
                        Log.e("LoginView - OnFailure", e.toString())
                        setErrorMessage("Por favor ingrese correctamente sus datos!")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            Log.d("LoginView - OnResponse", "Response successful!")

                            Log.d("LoginView - OnResponse", "Obtaining body")

                            val jwt = response.body!!.string()
                            Log.d("LoginView - OnResponse", "The JWT is: $jwt")
                            setJWT(jwt)
                            onJwtRetrieved(jwt)
                        }
                    }

                })
            }) {
                Text("Login")
            }
        }
    }
}

private data class LoginPayload(val email: String, val password: String);

@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    LoginAppTheme {
        LoginView(rememberNavController()) { _ -> }
    }
}