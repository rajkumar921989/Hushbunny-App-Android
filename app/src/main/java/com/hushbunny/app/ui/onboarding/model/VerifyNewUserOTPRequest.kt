package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants

@Keep
data class VerifyNewUserOTPRequest(
    val otpId: String,
    val otp: String,
    val deviceType:String = AppConstants.ANDROID
)
