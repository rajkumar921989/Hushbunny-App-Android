package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.onboarding.model.LoginResponse
import com.hushbunny.app.ui.service.UserActionService
import javax.inject.Inject

class UserActionRepositoryImpl @Inject constructor(private val userActionService: UserActionService) : UserActionRepository {
    override suspend fun updateUserAction(type: String): UserActionResponseModel {
        return userActionService.updateUserAction(type)
    }

    override suspend fun updateNotificationSettings(notificationSettingsRequest: NotificationSettingsRequest): LoginResponse {
        return userActionService.updateNotificationSettings(notificationSettingsRequest)
    }

    override suspend fun getUserDetail(): LoginResponse {
        return userActionService.getUserDetail()
    }

    override suspend fun editUserProfile(editProfileRequest: EditProfileRequest): LoginResponse {
        return userActionService.editUserProfile(editProfileRequest)
    }

    override suspend fun shareWithSpouse(inviteSpouseRequest: InviteSpouseRequest): BaseResponse {
        return userActionService.shareWithSpouse(inviteSpouseRequest)
    }

    override suspend fun sendEditProfileOTP(editProfileOtpRequest: EditProfileOtpRequest): BaseResponse {
        return userActionService.sendEditProfileOTP(editProfileOtpRequest)
    }

    override suspend fun reSendEditProfileOTP(): BaseResponse {
        return userActionService.reSendEditProfileOTP()
    }

    override suspend fun verifyEditProfileOTP(verifyEditProfileOtpRequest: VerifyEditProfileOtpRequest): BaseResponse {
        return userActionService.verifyEditProfileOTP(verifyEditProfileOtpRequest)
    }
}