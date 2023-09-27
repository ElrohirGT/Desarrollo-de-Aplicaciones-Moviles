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
fun RegisterView(navHostController: NavHostController) {
    val (username, setUsername) = remember { mutableStateOf("") }
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

    val (registered, setRegistered) = remember { mutableStateOf(false) }
    if (registered) {
        navHostController.popBackStack()
        setRegistered(false)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ) {
        Text("Registro", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(15.dp))

        Text("Username:")
        TextField(value = username, onValueChange = { setUsername(it) })
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
                Log.d("RegisterView - OnClick", "Creando request...")
                val request = Request.Builder()
                    .url("$HOST/register")
                    .post(
                        GSON.toJson(RegisterPayload(username, email, password))
                            .toRequestBody(MEDIA_TYPE_JSON)
                    )
                    .build()

                Log.d("RegisterView - OnClick", "Enviando request...")
                HTTP_CLIENT.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("RegisterView - OnFailure", "Ocurrió un error!")
                        Log.e("RegisterView - OnFailure", e.toString())
                        setErrorMessage("Ocurrió un error al crear el usuario!")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                Log.e("RegisterView - OnResponse", "Response was not successful!")
                                Log.e("RegisterView - OnResponse", response.message)
                                return;
                            }

                            Log.d("RegisterView - OnResponse", "Response successful!")
                            setRegistered(true)
                        }
                    }

                })
            }) {
                Text("Register")
            }
        }
    }
}

private data class RegisterPayload(val username: String, val email: String, val password: String)

@Preview(showBackground = true)
@Composable
fun RegisterViewPreview() {
    LoginAppTheme {
        RegisterView(rememberNavController())
    }
}