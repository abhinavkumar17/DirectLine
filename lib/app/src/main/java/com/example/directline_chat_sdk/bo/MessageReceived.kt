package com.example.directline_chat_sdk.bo

import java.io.Serializable

internal data class MessageReceived(val activities: List<MessageActivity>,
                                    val watermark: String)
    : Serializable