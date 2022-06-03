package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class KidsListResponseModel(
    val statusCode: Int,
    val message: String,
    val data: KidsData?
)

@Keep
data class KidsData(val listing: ArrayList<KidsResponseModel>)
