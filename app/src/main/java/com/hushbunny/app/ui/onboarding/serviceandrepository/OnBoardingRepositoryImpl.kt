package com.hushbunny.app.ui.onboarding.serviceandrepository

import com.hushbunny.app.ui.onboarding.model.*
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import javax.inject.Inject

sealed class LoginResponseStatus {
    data class Error(val message: String) : LoginResponseStatus()
    data class Success(val message: String, val loginResponse: UserData?) : LoginResponseStatus()
}

class OnBoardingRepositoryImpl @Inject constructor(private val onBoardingService: OnBoardingService) :
    OnBoardingRepository {
    override suspend fun userLogin(loginRequest: LoginRequest): LoginResponseStatus {
        val response = onBoardingService.userLogin(loginRequest = loginRequest)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> LoginResponseStatus.Success(response.message.orEmpty(), response.data)
            else -> LoginResponseStatus.Error(response.message.orEmpty())
        }
    }

    override suspend fun forgetUser(forgetPasswordRequest: ForgetPasswordRequest): OTPResponse {
        return onBoardingService.forgetUser(forgetPasswordRequest)
    }

    override suspend fun verifyForgetOTP(verifyForgetOTPRequest: VerifyForgetOTPRequest): PasswordResponse {
        return onBoardingService.verifyForgetOTP(verifyForgetOTPRequest)
    }

    override suspend fun resendOTP(resendOTPRequest: ResendOTPRequest): PasswordResponse {
        return onBoardingService.resendOTP(resendOTPRequest)
    }

    override suspend fun createPassword(createPasswordRequest: CreatePasswordRequest): PasswordResponse {
        return onBoardingService.createPassword(createPasswordRequest)
    }

    override suspend fun createNewAccount(createPasswordRequest: CreateNewAccountRequest): OTPResponse {
        return onBoardingService.createNewAccount(createPasswordRequest)
    }

    override suspend fun verifyNewAccountOTP(verifyNewUserOTPRequest: VerifyNewUserOTPRequest): NewUserResponse {
        return onBoardingService.verifyNewAccountOTP(verifyNewUserOTPRequest)
    }
}