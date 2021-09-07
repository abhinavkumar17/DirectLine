package com.example.directline_chatbot_sdk.bo

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object WebService
{

    val api by lazy {
        create()
    }

    private fun create(): API =
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://directline.botframework.com/v3/directline/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .build())
            .build()
            .create(API::class.java)

}