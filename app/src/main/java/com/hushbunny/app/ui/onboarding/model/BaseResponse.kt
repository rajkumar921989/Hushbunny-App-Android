package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep

@Keep
data class BaseResponse(
    val statusCode: Int?,
    val error: String?,
    val message: String?
)
