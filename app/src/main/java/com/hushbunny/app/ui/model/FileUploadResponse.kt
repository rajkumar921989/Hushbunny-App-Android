package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import okhttp3.MultipartBody
import okhttp3.RequestBody

@Keep
data class FileUploadResponse(val statusCode: Int, val message: String, val data: FileUploadData?)

@Keep
data class FileUploadData(val url: String)
