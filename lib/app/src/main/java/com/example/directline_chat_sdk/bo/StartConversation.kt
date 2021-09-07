package com.example.directline_chat_sdk.bo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

internal data class StartConversation(val conversationId: String,
                                      val token: String,
                                      @SerializedName("expires_in")
                                      val expiresIn: Long,
                                      val streamUrl: String)
    : Serializable