package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class BlockedUserListResponseModel(
    val statusCode: Int,
    val message: String,
    val data: BlockedUserData?
) : Serializable

@Keep
data class BlockedUserData(val listing: ArrayList<BlockedUserResponseModel>)
