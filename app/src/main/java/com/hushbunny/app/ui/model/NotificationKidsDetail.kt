package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class NotificationKidsDetail(
    val _id: String?,
    val name: String?,
    val nickName: String?,
    val gender: String?,
    val dob: String?,
    val birtCity: String?,
    val image: String?,
    val status: String?
)