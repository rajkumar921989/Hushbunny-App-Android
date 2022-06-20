package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class NotificationSettingsRequest(
    val important: Boolean,
    val optional: Boolean,
    val type: String
)
