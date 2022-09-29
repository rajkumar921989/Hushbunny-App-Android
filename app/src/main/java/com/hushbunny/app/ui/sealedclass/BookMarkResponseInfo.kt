package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.MomentListingModel

sealed class BookMarkResponseInfo {
    object ApiError : BookMarkResponseInfo()
    data class BookMarkFailure(val message: String) : BookMarkResponseInfo()
    data class BookMarkSuccess(val position:Int, val bookMark: MomentListingModel?) : BookMarkResponseInfo()
}
