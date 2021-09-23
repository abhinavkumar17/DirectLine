package com.example.directline_chatbot_sdk.bo

data class Activity(
    val attachments: List<Attachment>,
    val channelId: String,
    val conversation: Conversation,
    val entities: List<Any>,
    val from: From,
    val id: String,
    val replyToId: String,
    val text: String,
    val timestamp: String,
    val type: String
)