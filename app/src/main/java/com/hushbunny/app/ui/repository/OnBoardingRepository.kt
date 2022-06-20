package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.onboarding.model.*

interface OnBoardingRepository {
    suspend fun userLogin(loginRequest: LoginRequest) : LoginResponseStatus
    suspend fun forgetUser(forgetPasswordRequest: ForgetPasswordRequest) : OTPResponse
    suspend fun verifyForgetOTP(verifyForgetOTPRequest: VerifyForgetOTPRequest) : PasswordResponse
    suspend fun resendOTP(resendOTPRequest: ResendOTPRequest) : PasswordResponse
    suspend fun createPassword(createPasswordRequest: CreatePasswordRequest) : PasswordResponse
    suspend fun createNewAccount(createPasswordRequest: CreateNewAccountRequest) : OTPResponse
    suspend fun verifyNewAccountOTP(verifyNewUserOTPRequest: VerifyNewUserOTPRequest) : NewUserResponse
    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest) : PasswordResponse
}