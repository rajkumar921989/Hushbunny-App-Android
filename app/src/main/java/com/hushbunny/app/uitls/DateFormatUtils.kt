package com.hushbunny.app.uitls

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object DateFormatUtils {
    fun String.convertDateToISOFormat(): String? {
        return if (this.isNullOrEmpty()) null
        else {
            try {
                val currentFormat = SimpleDateFormat("dd MMM, yyyy")
                val date = currentFormat.parse(this)
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                simpleDateFormat.format(date)
            } catch (e: Exception) {
                ""
            }

        }

    }


    fun String.convertISODateIntoAppDateFormat(): String {
        return if (this.isNullOrEmpty()) ""
        else {
            try {
                val currentFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy")
                val date = currentFormat.parse(this)
                simpleDateFormat.format(date)
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun String.convertDateIntoAppDateFormat(): String {
        return if (this.isNullOrEmpty()) ""
        else {
            try {
                val currentFormat = SimpleDateFormat("dd/MM/yyyy")
                val date = currentFormat.parse(this)
                val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy")
                simpleDateFormat.format(date)
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun String.getTimeAgo(): String {
        return if (this.isNullOrEmpty())
            ""
        else {
            val currentFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            currentFormat.timeZone = TimeZone.getTimeZone("GMT")
            val time = currentFormat.parse(this).time
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            ago.toString()
        }
    }
}