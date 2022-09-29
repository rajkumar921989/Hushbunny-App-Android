package com.hushbunny.app.ui.model

import androidx.annotation.Keep

@Keep
data class BookMarkResponse(val statusCode: Int, val message: String, val data: MomentListingModel?)
