package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class AddKidRequest(
    val name: String,
    val gender: String,
    val dob: String? = null,
    val birthCountryISO2: String? = null,
    val birtCity: String? = null,
    val image: String? = null,
    val nickName: String? = null,
    val _id: String? = null
)
