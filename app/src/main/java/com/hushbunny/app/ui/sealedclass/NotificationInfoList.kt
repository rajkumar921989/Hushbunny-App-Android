package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.NotificationModel

sealed class NotificationInfoList {
    data class Error(val message: String) : NotificationInfoList()
    object NoList : NotificationInfoList()
    data class HaveList(val notificationList: List<NotificationModel>) : NotificationInfoList()
}
