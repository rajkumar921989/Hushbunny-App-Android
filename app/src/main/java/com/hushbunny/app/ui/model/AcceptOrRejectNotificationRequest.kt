package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AcceptOrRejectNotificationRequest(
    val shareId: String,
    val action: String
)
