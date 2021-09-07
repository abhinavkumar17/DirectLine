package com.example.directline_chatbot_sdk.bo

import java.io.Serializable
import com.google.gson.annotations.SerializedName

internal data class StartConversation(val conversationId: String,
                                      val token: String,
                                      @SerializedName("expires_in")
                                      val expiresIn: Long,
                                      val streamUrl: String)
    : Serializable