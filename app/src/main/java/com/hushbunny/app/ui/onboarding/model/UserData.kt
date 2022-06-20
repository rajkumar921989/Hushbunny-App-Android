package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep

@Keep
data class UserData(
    val token: String?,
    val image: String?,
    val name: String?,
    val email: String?,
    val _id: String?,
    val updatedAt: String?,
    val createdAt: String?,
    val userType: String?,
    val status: String?,
    val isProfile: String?,
    val callingCode: String?,
    val phoneNumber: String?,
    val fullNumber: String?,
    val dob: String?,
    val gender: String?,
    val associatedAs: String?,
    val countryId: String?,
    val inAppNotifications: InAppNotifications?,
    val emailNotifications: InAppNotifications?
)
