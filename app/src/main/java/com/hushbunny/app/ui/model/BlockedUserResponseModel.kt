package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.AppConstants
import java.io.Serializable

@Keep
data class BlockedUserResponseModel(
    val _id: String? = null,
    val name: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val callingCode: String? = null,
    val phoneNumber: String? = null,
    val fullNumber: String? = null,
    val displayId: String? = null,
    val userType: String? = null,
    val countryId: String? = null,
    val email: String? = null,
    val image: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
): Serializable
