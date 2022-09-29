package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class NotificationAPIResponseModel(val statusCode: Int, val message: String, val data: NotificationData?)

@Keep
data class NotificationData(val listing: List<NotificationListModel>)

@Keep
data class NotificationRecieverData(
    val _id: String?,
    val name: String?,
    val associatedAs: String?,
    val image: String?
)

@Keep
data class NotificationListModel(
    val _id: String?, val title: String?, val content: String?, val message: String?, val type: String?,
    val isRead: Boolean?, val senderId: NotificationSenderId?, val status: String?, val createdAt: String?,
    val updatedAt: String?, val shareId: NotificationShareId?, val recieverId: NotificationRecieverData?,
    val kidId: String?, val reactionId: NotificationReactionData?, val commentId: String?, val momentId: String?
)

@Keep
data class NotificationReactionData(
    val _id: String?,
    val momentId: String?,
    val reactedBy: String?,
    val emojiType: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@Keep
data class NotificationCommentData(
    val _id: String?,
    val momentId: String?,
    val commentBy: String?,
    val comment: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@Keep
data class NotificationMomentData(
    val _id: String?
)
@Keep
data class NotificationKidData(
    val _id: String?
)
