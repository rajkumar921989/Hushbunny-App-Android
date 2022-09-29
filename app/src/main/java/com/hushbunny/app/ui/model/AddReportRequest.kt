package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddReportRequest(
    val kidId: String? = null,
    val momentId: String? = null,
    val commentId: String? = null,
    val reason: String? = null,
    val reasonId: String? = null,
    val type: String? = null,
    val userId: String? = null
)
