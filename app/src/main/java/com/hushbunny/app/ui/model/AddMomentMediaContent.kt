package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddMomentMediaContent(
    val type: String,
    val text: String,
    val original: String,
    val thumbnail: String
)
