package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class EditedUserDetail(
    val name: String = "",
    val dateOfBirth: String = "",
    val country: String = "",
    val isPhoneNumberEdited: Boolean = false,
    val isEmailAndPhoneNumberEdited: Boolean = false,
    val phoneNumber: String = "",
    val callingCode: String = "",
    val isEmailEdited: Boolean = false,
    val email: String = "",
    val gender: String = "",
    val associatedWith: String = "",
    val image: String = ""
) : Serializable
