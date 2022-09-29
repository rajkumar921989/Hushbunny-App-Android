package com.hushbunny.app.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class FilterModel(
    var type: String, var date: String = "", var fromDate: String = "", var toDate: String = ""
) : Serializable
