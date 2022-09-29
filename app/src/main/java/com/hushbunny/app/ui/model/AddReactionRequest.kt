package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddReactionRequest(
    val emojiType: String,
    val momentId: String
)
