package com.hushbunny.app.providers

import android.app.Application
import androidx.annotation.BoolRes
import androidx.annotation.StringRes

class ResourceProviderImp constructor(private val app: Application) : ResourceProvider {

    override fun getString(@StringRes id: Int): String = app.getString(id)

    override fun getString(@StringRes id: Int, vararg arguments: Any): String = app.getString(id, *arguments)

    override fun getBoolean(@BoolRes id: Int): Boolean = app.resources.runCatching { getBoolean(id) }.getOrElse { false }

    override fun getInt(id: Int): Int = app.resources.runCatching { getInteger(id) }.getOrDefault(0)

    override fun getDimension(id: Int): Float = app.resources.runCatching { getDimension(id) }.getOrDefault(0f)
}
