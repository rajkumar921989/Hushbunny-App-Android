package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep

@Keep
data class LoginResponse(
    val statusCode: Int?,
    val error: String?,
    val message: String?,
    val responseType: String?,
    val data: UserData?
)

