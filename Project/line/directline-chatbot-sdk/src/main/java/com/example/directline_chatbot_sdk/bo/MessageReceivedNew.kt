package com.example.directline_chatbot_sdk.bo

data class MessageReceivedNew(
    val activities: List<Activity>,
    val watermark: String
)