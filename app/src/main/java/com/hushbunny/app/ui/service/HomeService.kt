package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse

interface HomeService {
    suspend fun getKidsList(): KidsListResponseModel
    suspend fun addNewKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel
    suspend fun getBlockedUserList(): BlockedUserListResponseModel
    suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BaseResponse
    suspend fun notificationList(queryParams: Map<String, Any>): NotificationAPIResponseModel
    suspend fun acceptOrRejectNotification(acceptOrRejectNotificationRequest: AcceptOrRejectNotificationRequest): BaseResponse
    suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse
    suspend fun unReadNotification(unReadNotificationRequest: UnReadNotificationRequest): BaseResponse
    suspend fun unReadNotificationCount(): NotificationUnreadCountModel
}