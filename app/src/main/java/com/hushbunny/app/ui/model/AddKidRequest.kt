package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddKidRequest(
    val name: String,
    val gender: String,
    val dob: String,
    val birthCountryISO2: String? = null,
    val birtCity: String? = null,
    val image: String? = null
)
