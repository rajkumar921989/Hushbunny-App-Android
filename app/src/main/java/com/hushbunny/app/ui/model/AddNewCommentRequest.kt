package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddNewCommentRequest(
    val comment: String,
    val momentId: String
)
