package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import java.io.Serializable

@Keep
data class KidsResponseModel(
    val _id: String? = null,
    val name: String? = null,
    val firstName: String? = null,
    val nickName: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val birthCountryISO2: String? = null,
    val birtCity: String? = null,
    val image: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: String? = null,
    val isSpauseAdded: Boolean? = null,
    val type: String = APIConstants.KIDS_LIST,
    var isSelected: Boolean = false
) : Serializable
