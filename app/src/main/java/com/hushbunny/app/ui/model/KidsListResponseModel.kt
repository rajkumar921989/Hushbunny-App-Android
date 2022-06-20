package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class KidsListResponseModel(
    val statusCode: Int,
    val message: String,
    val data: KidsData?
):Serializable

@Keep
data class KidsData(val listing: ArrayList<KidsResponseModel>)
