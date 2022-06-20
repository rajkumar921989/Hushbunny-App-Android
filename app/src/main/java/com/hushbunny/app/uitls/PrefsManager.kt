package com.hushbunny.app.uitls

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringDef
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicBoolean

class PrefsManager private constructor(context: Context) {
    private val sharedPrefName = "HushBunny"
    private val gson = Gson()
    private var preferences: SharedPreferences

    companion object {
        private lateinit var instance: PrefsManager
        private val isInitialized = AtomicBoolean()     // To check if instance was previously initialized or not

        fun initialize(context: Context) {
            if (!isInitialized.getAndSet(true)) {
                instance = PrefsManager(context.applicationContext)
            }
        }

        fun get(): PrefsManager = instance
    }

    init {
        preferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
    }

    fun saveStringValue(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun saveIntValues(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun saveBooleanValues(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun save(key: String, `object`: Any?) {
        if (`object` == null) {
            throw IllegalArgumentException("object is null")
        }

        // Convert the provided object to JSON string
        save(key, gson.toJson(`object`))
    }

    fun getString(key: String, defValue: String): String =
        preferences.getString(key, defValue).toString()

    fun getInt(key: String, defValue: Int): Int = preferences.getInt(key, defValue)

    fun getFloat(key: String, defValue: Float): Float = preferences.getFloat(key, defValue)

    fun getBoolean(key: String, defValue: Boolean): Boolean = preferences.getBoolean(key, defValue)

    fun <T> getObject(key: String, objectClass: Class<T>): T? {
        val jsonString = preferences.getString(key, null)
        return if (jsonString == null) {
            null
        } else {
            try {
                gson.fromJson(jsonString, objectClass)
            } catch (e: Exception) {
                throw IllegalArgumentException("Object stored with key $key is instance of other class")
            }
        }
    }

    fun removeAll() {
        preferences.edit().clear().apply()
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}