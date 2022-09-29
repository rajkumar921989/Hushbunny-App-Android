package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import java.io.Serializable

@Keep
data class ParentModel(
    val _id: String? = null,
    val name: String? = null,
    val associatedAs: String? = null,
    val firstName: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val image: String? = null
) : Serializable
