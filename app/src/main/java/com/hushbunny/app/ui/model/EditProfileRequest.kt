package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class EditProfileRequest(
    val name: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val countryId: String? = null,
    val phoneNumber: String? = null,
    val callingCode: String? = null,
    val email: String? = null,
    val associatedAs: String? = null
)
