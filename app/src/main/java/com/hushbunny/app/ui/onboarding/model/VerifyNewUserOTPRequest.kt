package com.hushbunny.app.ui.onboarding.model

import android.os.Build
import androidx.annotation.Keep
import com.hushbunny.app.BuildConfig
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import java.time.ZoneId

@Keep
data class VerifyNewUserOTPRequest(
    val otpId: String,
    val otp: String,
    val deviceType:String = APIConstants.ANDROID,
    val version: String = BuildConfig.VERSION_NAME,
    val model: String = Build.MODEL,
    val timeZone: String = ZoneId.systemDefault().toString()
)
