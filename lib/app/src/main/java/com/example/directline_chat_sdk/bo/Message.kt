package com.example.directline_chat_sdk.bo

import java.io.Serializable

internal data class Message (val type: String,
                             val from: Id,
                             val text: String)
    : Serializable