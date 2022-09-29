package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import com.hushbunny.app.ui.onboarding.model.UserData

@Keep
data class UserDetailResponse(
    val statusCode: Int?,
    val error: String?,
    val message: String?,
    val data: UserData?
)

