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


class AppConstants {
    companion object {
        const val ZERO = 0
        const val SPLASH_DELAY_DURATION: Long = 3000
        //        User Detail
        const val IS_USER_LOGGED_IN = "IS_USER_LOGGED_IN"
        const val USER_FIRST_NAME = "USER_FIRST_NAME"
        const val USER_NAME = "USER_NAME"
        const val USER_TOKEN = "USER_TOKEN"
        const val USER_PHONE_NUMBER = "USER_PHONE_NUMBER"
        const val USER_EMAIL = "USER_EMAIL"
        const val USER_ID = "USER_ID"
        const val USER_CALLING_CODE = "USER_CALLING_CODE"
        const val USER_GENDER = "USER_GENDER"
        const val USER_ASSOCIATE_AS = "USER_ASSOCIATE_AS"
        const val USER_DATE_OF_BIRTH = "USER_DATE_OF_BIRTH"
        const val USER_COUNTRY = "USER_COUNTRY"
        const val IN_APP_IMPORTANT_NOTIFICATION = "IN_APP_IMPORTANT_NOTIFICATION"
        const val IN_APP_OPTIONAL_NOTIFICATION = "IN_APP_OPTIONAL_NOTIFICATION"
        const val EMAIL_IMPORTANT_NOTIFICATION = "EMAIL_IMPORTANT_NOTIFICATION"
        const val EMAIL_OPTIONAL_NOTIFICATION = "EMAIL_OPTIONAL_NOTIFICATION"

        fun isValidEmail(email: String): Boolean {
            return (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
        }

        fun String.isHavingUpperCaseLetter(): Boolean {
            return this.contains("[A-Z]".toRegex())
        }

        fun String.isHavingLowerCaseLetter(): Boolean {
            return this.contains("[a-z]".toRegex())
        }

        fun String.isHavingNumber(): Boolean {
            return this.contains("[0-9]".toRegex())
        }

        fun String.isHavingSpecialCharacter(): Boolean {
            return this.contains("[~!@#$%^&*()_+{}\\[\\]:;,.<>/?-]".toRegex())
        }

        fun saveUserDetail(userData: UserData?) {
            if (userData?.token.orEmpty().isNotEmpty())
                PrefsManager.get().saveStringValue(USER_TOKEN, userData?.token.orEmpty())
            if (!userData?.name.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_NAME, userData?.name.orEmpty())
            if (!userData?.firstName.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_FIRST_NAME, userData?.firstName.orEmpty())
            if (!userData?._id.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_ID, userData?._id.orEmpty())
            if (!userData?.email.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_EMAIL, userData?.email.orEmpty())
            if (!userData?.phoneNumber.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_PHONE_NUMBER, userData?.phoneNumber.orEmpty())
            if (!userData?.callingCode.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_CALLING_CODE, userData?.callingCode.orEmpty())
            if (!userData?.dob.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_DATE_OF_BIRTH, userData?.dob.orEmpty())
            if (!userData?.countryId.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_COUNTRY, userData?.countryId.orEmpty())
            if (!userData?.associatedAs.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_ASSOCIATE_AS, userData?.associatedAs.orEmpty())
            if (!userData?.gender.isNullOrEmpty())
                PrefsManager.get().saveStringValue(USER_GENDER, userData?.gender.orEmpty())
            if (userData?.inAppNotifications?.important != null)
                PrefsManager.get().saveBooleanValues(IN_APP_IMPORTANT_NOTIFICATION, userData.inAppNotifications.important)
            if (userData?.inAppNotifications?.optional != null)
                PrefsManager.get().saveBooleanValues(IN_APP_OPTIONAL_NOTIFICATION, userData.inAppNotifications.optional)
            if (userData?.emailNotifications?.important != null)
                PrefsManager.get().saveBooleanValues(EMAIL_IMPORTANT_NOTIFICATION, userData.emailNotifications.important)
            if (userData?.emailNotifications?.optional != null)
                PrefsManager.get().saveBooleanValues(EMAIL_OPTIONAL_NOTIFICATION, userData.emailNotifications.optional)
        }

        fun getUserFirstName(): String {
            return PrefsManager.get().getString(USER_FIRST_NAME, "")
        }

        fun getSettingList(resourceProvider: ResourceProvider): List<SettingsModel> {
            return listOf(
                SettingsModel(
                    name = resourceProvider.getString(R.string.edit_profile),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_user
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.change_password),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_settings_change_password
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.kids_list),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_user
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.in_app_notifications_settings),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_setting_notification
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.email_settings),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_seeting_email
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.blocked_list),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_user
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.bookmarks),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_setting_book_mark
                ),
                SettingsModel(
                    name = resourceProvider.getString(R.string.important_moments),
                    description = resourceProvider.getString(R.string.edit_profile_description),
                    image = R.drawable.ic_setting_important
                )
            )

        }

        fun shareTheAPP(activity: Activity, title: String, extraMessage: String, appUrl: String) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, extraMessage)
            shareIntent.putExtra(Intent.EXTRA_TEXT, appUrl)
            activity.startActivity(Intent.createChooser(shareIntent, title))
        }

        fun navigateToLoginPage(activity: Activity) {
            PrefsManager.get().removeAll()
            val intent = Intent(activity, OnBoardingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(intent)
            activity.finish()
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

    }
}