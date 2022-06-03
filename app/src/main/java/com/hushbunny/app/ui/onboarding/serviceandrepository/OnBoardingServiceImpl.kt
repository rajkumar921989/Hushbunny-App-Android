package com.hushbunny.app.ui.onboarding.serviceandrepository

import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.network.NetworkCallHandler
import com.hushbunny.app.ui.onboarding.model.*
import com.hushbunny.app.uitls.AppConstants
import javax.inject.Inject

class OnBoardingServiceImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val networkCallHandler: NetworkCallHandler
) : OnBoardingService {

    override suspend fun userLogin(loginRequest: LoginRequest): LoginResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_login_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = loginRequest
        )
    }

    override suspend fun forgetUser(forgetPasswordRequest: ForgetPasswordRequest): OTPResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_forget_password_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = forgetPasswordRequest
        )
    }

    override suspend fun verifyForgetOTP(verifyForgetOTPRequest: VerifyForgetOTPRequest): PasswordResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_verify_forget_otp_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = verifyForgetOTPRequest
        )
    }

    override suspend fun resendOTP(resendOTPRequest: ResendOTPRequest): PasswordResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_resendOtp_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = resendOTPRequest
        )
    }

    override suspend fun createPassword(createPasswordRequest: CreatePasswordRequest): PasswordResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_create_new_password_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = createPasswordRequest
        )
    }

    override suspend fun createNewAccount(createPasswordRequest: CreateNewAccountRequest): OTPResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_send_otp_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = createPasswordRequest
        )
    }

    override suspend fun verifyNewAccountOTP(verifyNewUserOTPRequest: VerifyNewUserOTPRequest): NewUserResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_session_url)}${
                resourceProvider.getString(
                    R.string.env_verify_new_user_otp_url
                )
            }",
            headers = hashMapOf(AppConstants.ACCEPT_LANGUAGE to AppConstants.ENGLISH),
            queryParams = hashMapOf(),
            requestBody = verifyNewUserOTPRequest
        )
    }
}