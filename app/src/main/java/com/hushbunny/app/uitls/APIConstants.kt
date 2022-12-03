package com.hushbunny.app.uitls


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
        const val IS_FILTER_APPLIED = "IS_FILTER_APPLIED"
        const val FILTER_MODEL = "FILTER_MODEL"
        const val ACCEPTED = "ACCEPTED"
        const val REJECTED = "REJECTED"
        const val QUERY_PARAMS_PAGE = "page"
        const val QUERY_PARAMS_PER_PAGE = "perPage"
        const val QUERY_PARAMS_PER_PAGE_VALUE = 30
        const val QUERY_PARAMS_TYPE = "type"
        const val QUERY_PARAMS_KID_ID = "kidId"
        const val QUERY_PARAMS_USER_ID = "userId"
        const val QUERY_PARAMS_MOMENT_ID = "momentId"
        const val QUERY_PARAMS_COMMENT_ID = "commentId"
        const val QUERY_PARAMS_START_DATE = "startDate"
        const val QUERY_PARAMS_END_DATE = "endDate"
        const val QUERY_PARAMS_SORT = "sort"
        const val IMAGE_BASE_URL = "https://res.cloudinary.com/hushbunny/image/upload/c_fill,g_face,q_100/"
        const val VIDEO_BASE_URL = "https://res.cloudinary.com/hushbunny/video/upload/c_fill,g_face,q_100/"
        const val IS_SPOUSE_INVITED = "IS_SPOUSE_INVITED"


        fun getAuthorization(): String {
            return PrefsManager.get().getString(AppConstants.USER_TOKEN, "")
        }

    }
}