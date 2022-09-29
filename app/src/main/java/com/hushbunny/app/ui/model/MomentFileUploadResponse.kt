package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import okhttp3.MultipartBody
import okhttp3.RequestBody

@Keep
data class MomentFileUploadResponse(val statusCode: Int, val message: String, val data: List<MomentUploadData>?)

@Keep
data class MomentUploadData(val url: MomentURLData)

@Keep
data class MomentURLData(val original: String?, val type: String?, val thumbnail: String?)
