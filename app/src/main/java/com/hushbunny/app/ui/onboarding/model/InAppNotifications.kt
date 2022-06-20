package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep

@Keep
data class InAppNotifications(
    val mandatory: Boolean? = true,
    val important: Boolean? = false,
    val optional: Boolean? = false
)
