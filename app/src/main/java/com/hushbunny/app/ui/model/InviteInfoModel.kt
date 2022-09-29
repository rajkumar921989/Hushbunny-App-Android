package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class InviteInfoModel(
    val _id: String? = null,
    val callingCode: String? = null,
    val phoneNumber: String? = null,
    val fullNumber: String? = null,
    val email: String? = null,
    val shareBy: String? = null,
    val name: String? = null,
    val kidId: String? = null,
    val userId: String? = null,
    val invitedUserId: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: String? = null
) : Serializable
