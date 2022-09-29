package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.ReportListingModel

sealed class ReportReasonInfo {
    data class Error(val message: String) : ReportReasonInfo()
    object NoList : ReportReasonInfo()
    object ApiError : ReportReasonInfo()
    data class HaveList(val reasonList: List<ReportListingModel>) : ReportReasonInfo()
}
