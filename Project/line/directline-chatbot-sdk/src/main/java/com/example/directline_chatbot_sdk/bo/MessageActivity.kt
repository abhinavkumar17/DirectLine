package com.example.directline_chatbot_sdk.bo

import java.io.Serializable

internal data class MessageActivity(val type: String,
                                    val id: String,
                                    val timestamp: String,
                                    val localTimestamp: String,
                                    val channelId: String,
                                    val from: Id,
                                    val conversation: Id,
                                    val text: String,
                                    val inputHint: String,
                                    val replyToId: String)
    : Serializable