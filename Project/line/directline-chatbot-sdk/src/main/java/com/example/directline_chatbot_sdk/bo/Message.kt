package com.example.directline_chatbot_sdk.bo

import java.io.Serializable

internal data class Message (val type: String,
                             val from: Id,
                             val text: String)
    : Serializable