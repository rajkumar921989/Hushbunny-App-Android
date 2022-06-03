package com.hushbunny.app.providers

import androidx.annotation.AnyRes
import androidx.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes id: Int): String
    fun getString(@StringRes id: Int, vararg arguments: Any): String
    fun getBoolean(@AnyRes id: Int): Boolean
    fun getInt(@AnyRes id: Int): Int
    fun getDimension(@AnyRes id: Int): Float
}
