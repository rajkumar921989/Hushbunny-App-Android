package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddKidsResponseModel(
    val statusCode: Int,
    val message: String,
    val data: KidsResponseModel?
)
