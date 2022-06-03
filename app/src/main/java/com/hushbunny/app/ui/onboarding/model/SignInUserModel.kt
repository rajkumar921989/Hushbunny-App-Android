package com.hushbunny.app.ui.onboarding.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SignInUserModel(
    val name: String,
    val dateOfBirth: String,
    val gender: String,
    val relationShipWithKid: String,
    val country: String
) : Serializable
