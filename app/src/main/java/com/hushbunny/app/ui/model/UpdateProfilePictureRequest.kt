package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class UpdateProfilePictureRequest(
    val image: String,
    val kidId: String? = null,
    val type: String
)
