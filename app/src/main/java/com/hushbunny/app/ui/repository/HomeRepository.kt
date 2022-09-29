package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.sealedclass.MomentResponseInfo
import com.hushbunny.app.ui.sealedclass.NotificationInfoList

interface HomeRepository {
    suspend fun getKidsList(): KidsStatusInfo
    suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel
    suspend fun getBlockedUserList(): BlockedUserList
    suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BlockedUserList
    suspend fun notificationList(queryParams: Map<String, Any>): NotificationInfoList
    suspend fun acceptOrRejectNotification(acceptOrRejectNotificationRequest: AcceptOrRejectNotificationRequest): BaseResponse
    suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse
    suspend fun blockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BaseResponse
    suspend fun unReadNotification(unReadNotificationRequest: UnReadNotificationRequest): BaseResponse
    suspend fun unReadNotificationCount(): NotificationUnreadCountModel
}