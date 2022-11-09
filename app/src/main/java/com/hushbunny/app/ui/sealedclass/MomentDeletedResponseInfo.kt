package com.hushbunny.app.ui.sealedclass

sealed class MomentDeletedResponseInfo {
    object ApiError : MomentDeletedResponseInfo()
    data class HaveError(val message: String) : MomentDeletedResponseInfo()
    data class MomentDelete(val position: Int) : MomentDeletedResponseInfo()
}
