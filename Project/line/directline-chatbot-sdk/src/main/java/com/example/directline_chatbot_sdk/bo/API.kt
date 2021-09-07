package com.example.directline_chatbot_sdk.bo

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

internal interface API {
    @POST("conversations")
    fun startConversation(@Header("Authorization") secret: String): Call<StartConversation>

    @POST("conversations/{conversationId}/activities")
    fun send(@Body message: Message, @Path("conversationId") conversationId: String, @Header("Authorization") secret: String): Call<Id>

}