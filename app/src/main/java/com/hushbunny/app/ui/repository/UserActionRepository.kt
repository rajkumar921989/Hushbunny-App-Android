package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.onboarding.model.LoginResponse

interface UserActionRepository {
    suspend fun updateUserAction(type:String) : UserActionResponseModel
    suspend fun updateNotificationSettings(notificationSettingsRequest: NotificationSettingsRequest) : LoginResponse
    suspend fun getUserDetail() : LoginResponse
    suspend fun editUserProfile(editProfileRequest: EditProfileRequest) : LoginResponse
    suspend fun shareWithSpouse(inviteSpouseRequest: InviteSpouseRequest) : BaseResponse
    suspend fun sendEditProfileOTP(editProfileOtpRequest: EditProfileOtpRequest): BaseResponse
    suspend fun reSendEditProfileOTP(): BaseResponse
    suspend fun verifyEditProfileOTP(verifyEditProfileOtpRequest: VerifyEditProfileOtpRequest): BaseResponse
}