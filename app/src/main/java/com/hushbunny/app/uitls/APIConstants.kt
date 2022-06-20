package com.hushbunny.app.uitls

import android.app.Activity
import android.content.Intent
import android.util.Patterns
import androidx.core.content.ContextCompat.startActivity
import com.hushbunny.app.HomeActivity
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.SettingsModel
import com.hushbunny.app.ui.onboarding.OnBoardingActivity
import com.hushbunny.app.ui.onboarding.model.UserData


class APIConstants {
    companion object {
        const val API_RESPONSE_200 = 200
        const val UNAUTHORIZED_CODE = 401
        const val ANDROID = "ANDROID"
        const val EMAIL = "EMAIL"
        const val SUCCESS = "SUCCESS"
        const val PHONE_NUMBER = "PHONENUMBER"
        const val ACCEPT_LANGUAGE = "accept-language"
        const val AUTHORIZATION = "authorization"
        const val ENGLISH = "en"
        const val FORGOT = "FORGOT"
        const val REGISTRATION = "REGISTRATION"
        const val MALE = "Male"
        const val FEMALE = "Female"
        const val KIDS_LIST = "KIDS_LIST"
        const val ADD_KID = "ADD_KID"
        const val IN_APP = "IN_APP"
        const val UNBLOCKED = "UNBLOCKED"
        const val BLOCKED = "BLOCKED"
        const val MOTHER = "MOTHER"
        const val FATHER = "FATHER"
        const val IS_REQUIRED_API_CALL = "IS_REQUIRED_API_CALL"
        const val ACCEPTED = "ACCEPTED"
        const val REJECTED = "REJECTED"
        const val QUERY_PARAMS_PAGE = "page"
        const val QUERY_PARAMS_PER_PAGE = "perPage"
        const val QUERY_PARAMS_PER_PAGE_VALUE = 10
        const val QUERY_PARAMS_TYPE = "type"

        fun getAuthorization(): String {
            return PrefsManager.get().getString(AppConstants.USER_TOKEN, "")
        }

    }
}