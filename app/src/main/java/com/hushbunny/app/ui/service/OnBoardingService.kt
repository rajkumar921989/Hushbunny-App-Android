package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.onboarding.model.*

interface OnBoardingService {
    suspend fun userLogin(loginRequest: LoginRequest) : LoginResponse
    suspend fun forgetUser(forgetPasswordRequest: ForgetPasswordRequest) : OTPResponse
    suspend fun verifyForgetOTP(verifyForgetOTPRequest: VerifyForgetOTPRequest) : PasswordResponse
    suspend fun resendOTP(resendOTPRequest: ResendOTPRequest) : PasswordResponse
    suspend fun createPassword(createPasswordRequest: CreatePasswordRequest) : PasswordResponse
    suspend fun createNewAccount(createPasswordRequest: CreateNewAccountRequest) : OTPResponse
    suspend fun verifyNewAccountOTP(verifyNewUserOTPRequest: VerifyNewUserOTPRequest) : NewUserResponse
    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest) : PasswordResponse
}