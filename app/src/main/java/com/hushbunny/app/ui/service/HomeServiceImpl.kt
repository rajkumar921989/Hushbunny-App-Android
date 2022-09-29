package com.hushbunny.app.ui.service

import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.network.NetworkCallHandler
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.uitls.APIConstants
import javax.inject.Inject

class HomeServiceImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val networkCallHandler: NetworkCallHandler
) : HomeService {

    override suspend fun getKidsList(): KidsListResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_kids_list_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            )
        )
    }

    override suspend fun addNewKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel {
        return if (isEditKid) {
            networkCallHandler.putDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_edit_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addKidRequest
            )
        } else {
            networkCallHandler.postDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_add_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addKidRequest
            )
        }
    }

    override suspend fun getBlockedUserList(): BlockedUserListResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_blocked_user_list_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            )
        )
    }

    override suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BaseResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_block_unblock_user_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = blockUnBlockUserRequest
        )
    }

    override suspend fun notificationList(queryParams: Map<String, Any>): NotificationAPIResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_notification_url)}${resourceProvider.getString(R.string.env_kids_list_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun acceptOrRejectNotification(acceptOrRejectNotificationRequest: AcceptOrRejectNotificationRequest): BaseResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_spouse_url)}${resourceProvider.getString(R.string.env_accept_reject_invite_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = acceptOrRejectNotificationRequest
        )
    }

    override suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse {
        return if (isEdit) {
            networkCallHandler.putDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_edit_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addMomentRequest
            )
        } else {
            networkCallHandler.postDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_add_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addMomentRequest
            )
        }
    }

    override suspend fun unReadNotification(unReadNotificationRequest: UnReadNotificationRequest): BaseResponse {
        return  networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_notification_url)}${resourceProvider.getString(R.string.env_unread_notification_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = unReadNotificationRequest
        )
    }

    override suspend fun unReadNotificationCount(): NotificationUnreadCountModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_notification_url)}${resourceProvider.getString(R.string.env_unread_notification_count_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = hashMapOf()
        )
    }
}