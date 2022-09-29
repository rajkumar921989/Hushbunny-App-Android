package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class ReactionResponseModel(val statusCode: Int, val message: String, val data: ReactionDataModel?)

@Keep
data class ReactionDataModel(val listing: List<ReactionModel>, val headerCounts: ReactionCountModel?)

@Keep
data class ReactionModel(
    val _id: String?,
    val momentId: String?,
    val reactedBy: ReactedByModel?,
    val emojiType: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@Keep
data class ReactedByModel(
    val _id: String?,
    val name: String?,
    val dob: String?,
    val gender: String?,
    val associatedAs: String?,
    val image: String?,
)

@Keep
data class ReactionCountModel(
    val all: String? = null,
    val heart: String? = null,
    val laugh: String? = null,
    val sad: String? = null
)