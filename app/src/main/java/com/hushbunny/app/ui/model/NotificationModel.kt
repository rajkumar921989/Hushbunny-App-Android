package com.hushbunny.app.ui.model

data class NotificationModel(
    val type: String, val title: String, val message: String, val content: String, val image: String, val emojiType: String,
    val dateTime: String, val shareId: String, val kidName: String, val status: String, val momentID: String,
    val commentId: String, val kidID: String, val isRequiredHushBunnyLogo: Boolean, val isRead: Boolean, val notificationId: String
)
