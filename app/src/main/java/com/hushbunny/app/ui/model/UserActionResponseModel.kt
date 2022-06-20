package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class UserActionResponseModel(
    val statusCode: Int,
    val error: String,
    val message: String
)
