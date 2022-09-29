package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class MomentDetailResponseModel(val statusCode: Int, val message: String, val data: MomentDetailDataModel?)


@Keep
data class MomentDetailDataModel(
    val _id: String?,
    val momentDate: String?,
    val momentCount: String? = null,
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
    val comments: List<MomentCommentModel>?
) : Serializable

