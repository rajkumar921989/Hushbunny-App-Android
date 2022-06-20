package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class BlockUnBlockUserRequest(
    val userId: String,
    val action: String
)
