package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.ReactionCountModel
import com.hushbunny.app.ui.model.ReactionModel

sealed class ReactionResponseInfo {
    object ApiError : ReactionResponseInfo()
    data class NoReaction(val reactionCount: ReactionCountModel) : ReactionResponseInfo()
    data class HaveError(val message: String) : ReactionResponseInfo()
    data class HaveReactionList(val reactionCount: ReactionCountModel, val CommentList: List<ReactionModel>) : ReactionResponseInfo()
}
