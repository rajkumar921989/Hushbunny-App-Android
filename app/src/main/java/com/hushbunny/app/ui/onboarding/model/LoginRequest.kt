package com.hushbunny.app.ui.onboarding.model

import android.os.Build
import androidx.annotation.Keep
import com.hushbunny.app.BuildConfig
import com.hushbunny.app.uitls.APIConstants
import java.time.ZoneId

@Keep
data class LoginRequest(
    val loginBy: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val callingCode: String? = null,
    val password: String,
    val deviceToken: String? = null,
    val deviceType: String = APIConstants.ANDROID,
    val version: String = BuildConfig.VERSION_NAME,
    val model: String = Build.MODEL,
    val timeZone: String = ZoneId.systemDefault().toString(),
    val deviceID: String
)
