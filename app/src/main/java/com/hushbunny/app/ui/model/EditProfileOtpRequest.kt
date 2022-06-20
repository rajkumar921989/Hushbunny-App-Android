package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class EditProfileOtpRequest(
    val editFor: String,
    val phoneNumber: String? = null,
    val callingCode: String? = null,
    val email: String? = null
)
