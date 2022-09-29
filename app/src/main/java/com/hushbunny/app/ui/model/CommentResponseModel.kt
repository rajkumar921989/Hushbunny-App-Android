package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class CommentResponseModel(val statusCode: Int, val message: String, val data: CommentDataModel?)

@Keep
data class CommentDataModel(val listing: List<CommentModel>)

@Keep
data class CommentModel(
    val _id: String?,
    val momentId: CommentMomentIdModel?,
    val commentBy: CommentByModel?,
    val comment: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@Keep
data class CommentByModel(
    val _id: String?,
    val name: String?,
    val dob: String?,
    val gender: String?,
    val associatedAs: String?,
    val image: String?
)

@Keep
data class CommentMomentIdModel(
    val _id: String?,
    val addedBy: CommentMomentAddedByModel?
)

@Keep
data class CommentMomentAddedByModel(
    val _id: String?,
    val name: String?,
    val dob: String?,
    val gender: String?,
    val associatedAs: String?,
    val image: String?,
    val firstName: String?
)