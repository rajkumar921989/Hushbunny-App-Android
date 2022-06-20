package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants

@Keep
data class CreateNewAccountRequest(
    val registrationBy: String,
    val name: String,
    val dob: String? = null,
    val gender: String,
    val associatedAs: String,
    val countryId: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val callingCode: String? = null,
    val password: String
)
