package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class ReportResponseModel(val statusCode: Int, val message: String, val data: ReportDataModel?)

@Keep
data class ReportDataModel(val listing: List<ReportListingModel>?)

@Keep
data class ReportListingModel(val _id: String?, val reason: String, var isSelected: Boolean? = false)
