package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants

@Keep
data class LoginRequest(
    val loginBy: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val callingCode: String? = null,
    val password: String,
    val deviceType: String = AppConstants.ANDROID
)
