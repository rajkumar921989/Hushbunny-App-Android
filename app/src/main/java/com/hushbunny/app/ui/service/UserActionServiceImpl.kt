package com.hushbunny.app.ui.service

import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.network.NetworkCallHandler
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.onboarding.model.LoginResponse
import com.hushbunny.app.uitls.APIConstants
import javax.inject.Inject

class UserActionServiceImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val networkCallHandler: NetworkCallHandler
) : UserActionService {

    override suspend fun updateUserAction(type: String): UserActionResponseModel {
        val actionUrl = when (type) {
            resourceProvider.getString(R.string.deactivate_account) -> resourceProvider.getString(R.string.env_deactivate_url)
            resourceProvider.getString(R.string.delete_account) -> resourceProvider.getString(R.string.env_delete_url)
            else -> resourceProvider.getString(R.string.env_logout_url)
        }
        return networkCallHandler.deleteDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                actionUrl
            }",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            queryParams = hashMapOf()
        )
    }

    override suspend fun updateNotificationSettings(notificationSettingsRequest: NotificationSettingsRequest): LoginResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_notification_setting_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = notificationSettingsRequest
        )
    }

    override suspend fun getUserDetail(): LoginResponse {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_get_user_detail_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            )
        )
    }

    override suspend fun editUserProfile(editProfileRequest: EditProfileRequest): LoginResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_edit_profile_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = editProfileRequest
        )
    }

    override suspend fun updateProfilePicture(updateProfilePictureRequest: UpdateProfilePictureRequest): LoginResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_update_profile_picture_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = updateProfilePictureRequest
        )
    }

    override suspend fun shareWithSpouse(inviteSpouseRequest: InviteSpouseRequest): BaseResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_spouse_url)}${resourceProvider.getString(R.string.env_invite_spouse_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = inviteSpouseRequest
        )
    }

    override suspend fun sendEditProfileOTP(editProfileOtpRequest: EditProfileOtpRequest): BaseResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_send_edit_profile_otp_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = editProfileOtpRequest
        )
    }

    override suspend fun reSendEditProfileOTP(): BaseResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_resend_edit_profile_otp_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            )
        )
    }

    override suspend fun verifyEditProfileOTP(verifyEditProfileOtpRequest: VerifyEditProfileOtpRequest): BaseResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_verify_edit_profile_otp_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = verifyEditProfileOtpRequest
        )
    }

    override suspend fun updateDeviceToken(updateDeviceTokenRequest: UpdateDeviceTokenRequest): BaseResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${resourceProvider.getString(R.string.env_update_device_token_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = updateDeviceTokenRequest
        )
    }

    override suspend fun reSendInvite(invitationId: String): BaseResponse {
        return networkCallHandler.patchData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_spouse_url)}${resourceProvider.getString(R.string.env_resend_invite_url)}$invitationId",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), requestBody = ""
        )
    }
}