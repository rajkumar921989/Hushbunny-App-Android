package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.onboarding.model.LoginResponse

interface UserActionService {
    suspend fun updateUserAction(type: String): UserActionResponseModel
    suspend fun updateNotificationSettings(notificationSettingsRequest: NotificationSettingsRequest): LoginResponse
    suspend fun getUserDetail(): LoginResponse
    suspend fun editUserProfile(editProfileRequest: EditProfileRequest): LoginResponse
    suspend fun updateProfilePicture(updateProfilePictureRequest: UpdateProfilePictureRequest): LoginResponse
    suspend fun shareWithSpouse(inviteSpouseRequest: InviteSpouseRequest): BaseResponse
    suspend fun sendEditProfileOTP(editProfileOtpRequest: EditProfileOtpRequest): BaseResponse
    suspend fun reSendEditProfileOTP(): BaseResponse
    suspend fun verifyEditProfileOTP(verifyEditProfileOtpRequest: VerifyEditProfileOtpRequest): BaseResponse
    suspend fun updateDeviceToken(updateDeviceTokenRequest: UpdateDeviceTokenRequest): BaseResponse
    suspend fun reSendInvite(invitationId: String): BaseResponse
}