package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.CommentModel

sealed class CommentResponseInfo {
    object ApiError : CommentResponseInfo()
    object NoComment : CommentResponseInfo()
    data class HaveError(val message: String) : CommentResponseInfo()
    data class HaveCommentList(val type: String, val CommentList: List<CommentModel>) : CommentResponseInfo()
}
