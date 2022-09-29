package com.hushbunny.app.ui.repository

import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.sealedclass.NotificationInfoList
import com.hushbunny.app.ui.service.HomeService
import com.hushbunny.app.uitls.APIConstants
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(private val resourceProvider: ResourceProvider, private val homeService: HomeService) : HomeRepository {

    override suspend fun getKidsList(): KidsStatusInfo {
        val responseModel = homeService.getKidsList()
        val kidsList = arrayListOf<KidsResponseModel>()
        kidsList.add(addNewKid())
        return when (responseModel.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (responseModel.data?.listing.orEmpty().isNotEmpty()) {
                    kidsList.addAll(responseModel.data?.listing.orEmpty())
                }
                KidsStatusInfo.UserList(kidsList)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                KidsStatusInfo.ApiError
            }
            else -> {
                KidsStatusInfo.UserList(kidsList)
            }
        }
    }

    private fun addNewKid(): KidsResponseModel {
        return KidsResponseModel(type = APIConstants.ADD_KID, name = "Add kid")
    }

    override suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel {
        return homeService.addNewKid(isEditKid, addKidRequest)
    }

    override suspend fun getBlockedUserList(): BlockedUserList {
        val responseModel = homeService.getBlockedUserList()
        return when (responseModel.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (responseModel.data?.listing.orEmpty().isNotEmpty()) {
                    BlockedUserList.UserList(responseModel.data?.listing.orEmpty())
                } else {
                    BlockedUserList.NoList
                }
            }
            else -> {
                BlockedUserList.Error(responseModel.message)
            }
        }
    }

    override suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BlockedUserList {
        val response = homeService.blockUnBlockUser(blockUnBlockUserRequest)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                getBlockedUserList()
            }
            else -> {
                BlockedUserList.Error(response.message.orEmpty())
            }
        }

    }

    override suspend fun notificationList(queryParams: Map<String, Any>): NotificationInfoList {
        val response = homeService.notificationList(queryParams)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (response.data?.listing.orEmpty().isNotEmpty()) {
                    val notificationList = arrayListOf<NotificationModel>()
                    response.data?.listing?.forEach { model ->
                        notificationList.add(
                            NotificationModel(
                                notificationId = model._id.orEmpty(),
                                type = model.type.orEmpty(),
                                title = model.title.orEmpty(),
                                content = model.content.orEmpty(),
                                emojiType = model.reactionId?.emojiType.orEmpty(),
                                message = model.message.orEmpty(),
                                image = model.senderId?.image.orEmpty(),
                                dateTime = model.createdAt.orEmpty(),
                                shareId = model.shareId?._id.orEmpty(),
                                kidName = model.shareId?.kidId?.name.orEmpty(),
                                status = model.shareId?.status.orEmpty(),
                                momentID = model.momentId.orEmpty(),
                                commentId = model.commentId.orEmpty(),
                                kidID = model.kidId.orEmpty(), isRequiredHushBunnyLogo = model.senderId == null, isRead = model.isRead ?: false
                            )
                        )
                    }
                    NotificationInfoList.HaveList(notificationList.toList())
                } else {
                    NotificationInfoList.NoList
                }
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                NotificationInfoList.Error(response.message)
            }
            else -> {
                NotificationInfoList.NoList
            }
        }
    }

    override suspend fun acceptOrRejectNotification(acceptOrRejectNotificationRequest: AcceptOrRejectNotificationRequest): BaseResponse {
        return homeService.acceptOrRejectNotification(acceptOrRejectNotificationRequest)
    }

    override suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse {
        return homeService.addOREditMoment(isEdit = isEdit, addMomentRequest = addMomentRequest)
    }

    override suspend fun blockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BaseResponse {
        return homeService.blockUnBlockUser(blockUnBlockUserRequest)
    }

    override suspend fun unReadNotification(unReadNotificationRequest: UnReadNotificationRequest): BaseResponse {
        return homeService.unReadNotification(unReadNotificationRequest)
    }

    override suspend fun unReadNotificationCount(): NotificationUnreadCountModel {
        return homeService.unReadNotificationCount()
    }
}