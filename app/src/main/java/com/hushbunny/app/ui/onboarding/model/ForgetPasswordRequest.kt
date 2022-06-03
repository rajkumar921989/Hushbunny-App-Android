package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants

@Keep
data class ForgetPasswordRequest(
    val forgotBy: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val callingCode: String? = null
)
