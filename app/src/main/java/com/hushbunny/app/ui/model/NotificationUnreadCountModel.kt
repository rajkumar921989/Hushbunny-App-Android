package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class NotificationUnreadCountModel(
    var statusCode: Int?, var message: String?, var data: NotificationUnreadCountData?
)

@Keep
data class NotificationUnreadCountData(
    val unreadCount: Int?
)
