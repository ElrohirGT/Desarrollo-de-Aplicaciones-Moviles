package com.uvg.gt.loginapp

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

data class GetUserInfoPayload(val token: String)
data class UserInfoResponse(
    val user_id: String,
    val expire_date: String,
    val email: String,
    val username: String
)

@Composable
fun HomeView(navHostController: NavHostController, jwt: String) {
    val defaultUserInfo: UserInfoResponse? = null
    val (userInfo, setUserInfo) = remember { mutableStateOf(defaultUserInfo) }

    val defaultErrorMessage: String? = null
    val (errorMessage, setErrorMessage) = remember { mutableStateOf(defaultErrorMessage) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Log.e("HomeView - MainComponent", "El JWT reunido $jwt")
        if (errorMessage != null) {
            Log.e("HomeView - MainComponent", "El JWT es null!")
            AlertDialog(onDismissRequest = { /*TODO*/ }, confirmButton = {
                ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = { setErrorMessage(null) }) {
                        Text("Go back")
                    }
                }
            })
        } else if (userInfo != null) {

            Text("Id: ${userInfo.user_id}")
            Text("Username: ${userInfo.username}")
            Text("Email: ${userInfo.email}")
            Text("Expire Date: ${userInfo.expire_date}")
            Button(onClick = { navHostController.navigate("Main") }) {
                Text(text = "LogOut")
            }

        } else {
            Button(onClick = {
                Log.d("HomeView - OnClick", "Creando request...")
                Log.d("HomeView - OnClick", "The JWT is: $jwt")

                val request = Request.Builder()
                    .url("$HOST/user/info")
                    .post(
                        GSON.toJson(GetUserInfoPayload(jwt!!))
                            .toRequestBody(MEDIA_TYPE_JSON)
                    )
                    .build()

                Log.d("HomeView - OnClick", "Enviando request...")
                HTTP_CLIENT.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("HomeView - OnFailure", "Ocurrió un error!")
                        Log.e("HomeView - OnFailure", e.toString())
                        setErrorMessage("Por favor ingrese correctamente sus datos!")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            Log.d("HomeView - OnResponse", "Response successful!")
                            Log.d("HomeView - OnResponse", "Obtaining body")

                            val responseBody = response.body!!.string()
                            val userInfo = GSON.fromJson(responseBody, UserInfoResponse::class.java)
                            setUserInfo(userInfo)

                            Log.d("HomeView - OnResponse", "The user info in: $userInfo")
                        }
                    }
                })
            }) {
                Text("Load data...")
            }
            Button(onClick = { navHostController.navigate("Main") }) {
                Text(text = "LogOut")
            }
        }
    }
}