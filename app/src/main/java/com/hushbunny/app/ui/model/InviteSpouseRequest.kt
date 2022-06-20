package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class InviteSpouseRequest(
    val shareBy: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val callingCode: String? = null,
    val name: String? = null,
    val kidId: String? = null
)
