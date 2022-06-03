package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep

@Keep
data class OTPResponse(
    val statusCode: Int?,
    val error: String?,
    val message: String?,
    val data: OTPData?
)

@Keep
data class OTPData(val otpId: String?)