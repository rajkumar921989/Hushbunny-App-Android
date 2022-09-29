package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class NotificationShareId(
    val _id: String?,
    val fullNumber: String?,
    val shareBy: String?,
    val name: String?,
    val userId: String?,
    val status: String?,
    val kidId: NotificationKidsDetail?
)