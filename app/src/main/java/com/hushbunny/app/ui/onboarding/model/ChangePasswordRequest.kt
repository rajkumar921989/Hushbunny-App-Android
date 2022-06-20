package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants

@Keep
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
