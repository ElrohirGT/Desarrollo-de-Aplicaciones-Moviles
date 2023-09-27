package com.uvg.gt.loginapp

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

val HOST = "http://10.0.2.2:3000"
val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
val HTTP_CLIENT = OkHttpClient()
val GSON = Gson()