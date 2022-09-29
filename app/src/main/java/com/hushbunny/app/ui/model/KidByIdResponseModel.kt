package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import java.io.Serializable

@Keep
data class KidByIdResponseModel(
    val statusCode: Int, val message: String, val data: KidsResponseModel?
)
