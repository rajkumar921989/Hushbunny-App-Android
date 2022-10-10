package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class MomentResponseModel(val statusCode: Int, val message: String, val data: MomentDataModel?)

@Keep
data class MomentDataModel(val listing: List<MomentListingModel>, val count: String?)

@Keep
data class MomentListingModel(
    val _id: String?,
    val momentDate: String?,
    val momentNumber: String? = null,
    var reactionCount: String?,
    val commentCount: String?,
    val shortLink: String?,
    var isImportant: Boolean?,
    val isAddedBySpouse: Boolean? = false,
    var isBookmarked: Boolean?,
    val description: String?,
    val addedBy: MomentAddedByModel?,
    val kidId: List<MomentKidsModel>?,
    val parents: List<MomentParentModel>?,
    val mediaContent: List<MomentMediaModel>?,
    val createdAt: String?,
    val updatedAt: String?,
    var isReacted: Boolean?,
    var reactedInfo: MomentReactionModel?,
    val comments: List<MomentCommentModel>?,
    val otherUserReaction: OtherUserReactionModel?,
    val otherUserComment: MomentCommentModel?
) : Serializable

@Keep
data class MomentCommentModel(
    val _id: String?,
    val momentId: String?,
    val commentBy: CommentByModel?,
    val comment: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)
@Keep
data class MomentAddedByModel(
    val _id: String?,
    val name: String?,
    val dob: String?,
    val gender: String?,
    val associatedAs: String,
    val image: String?,
    val firstName: String?
) : Serializable

@Keep
data class MomentKidsModel(
    val _id: String? = null,
    val name: String? = null,
    val firstName: String? = null,
    val nickName: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val birthCountryISO2: String? = null,
    val birtCity: String? = null,
    val image: String? = null,
) : Serializable

@Keep
data class MomentMediaModel(
    val type: String? = null,
    val text: String? = null,
    val original: String? = null,
    val thumbnail: String? = null,
    val _id: String? = null,
    var isUploaded: Boolean = true,
    var isWebLinkImage: Boolean = false
) : Serializable

@Keep
data class MomentParentModel(
    val _id: String?,
    val name: String?,
    val dob: String?,
    val gender: String?,
    val associatedAs: String,
    val image: String?
) : Serializable

@Keep
data class MomentReactionModel(
    val _id: String?,
    val momentId: String?,
    val reactedBy: String?,
    val emojiType: String?,
    val status: String?,
    val createdAt: String,
    val updatedAt: String?
) : Serializable

@Keep
data class OtherUserReactionModel(
    val _id: String?,
    val momentId: String?,
    val reactedBy: OtherUserReactedByModel?,
    val emojiType: String?,
) : Serializable

@Keep
data class OtherUserReactedByModel(
    val _id: String?,
    val name: String?,
) : Serializable