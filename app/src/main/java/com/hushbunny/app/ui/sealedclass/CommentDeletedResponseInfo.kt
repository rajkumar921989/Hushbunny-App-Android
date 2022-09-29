package com.hushbunny.app.ui.sealedclass

sealed class CommentDeletedResponseInfo {
    object ApiError : CommentDeletedResponseInfo()
    data class HaveError(val message: String) : CommentDeletedResponseInfo()
    data class CommentDelete(val position: Int) : CommentDeletedResponseInfo()
}
