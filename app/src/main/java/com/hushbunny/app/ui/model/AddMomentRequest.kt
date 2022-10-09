package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddMomentRequest(
    val kidId: ArrayList<String>,
    val momentId: String? = null,
    val momentDate: String,
    val description: String?,
    val isImportant: Boolean,
    val mediaContent: List<AddMomentMediaRequest>
)

@Keep
data class AddMomentMediaRequest(
    val type: String? = null,
    val text: String? = null,
    val original: String? = null,
    val thumbnail: String? = null,
)

@Keep
data class AddMomentImageUrl(
    val link: String,
    val fileName: String
)