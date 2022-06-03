package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants

@Keep
data class PasswordResponse(
    val statusCode: Int?,
    val error: String?,
    val message: String?
)
