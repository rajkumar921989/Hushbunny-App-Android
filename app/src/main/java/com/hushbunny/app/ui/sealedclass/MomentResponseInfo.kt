package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.MomentListingModel

sealed class MomentResponseInfo {
    object ApiError : MomentResponseInfo()
    object NoList : MomentResponseInfo()
    data class MomentList(val count: String, val momentList: List<MomentListingModel>) : MomentResponseInfo()
}
