package com.hushbunny.app.uitls

import android.annotation.SuppressLint
import android.text.format.DateUtils
import com.hushbunny.app.ui.enumclass.FilterType
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("SimpleDateFormat")
object DateFormatUtils {
    const val FILTER_DATE_FORMAT = "dd MMM, yyyy"
    const val FILTER_MONTH_FORMAT = "MMM, yyyy"
    const val FILTER_YEAR_FORMAT = "yyyy"
    const val ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    fun String.convertDateToISOFormat(): String? {
        return if (this.isNullOrEmpty()) null
        else {
            try {
                val currentFormat = SimpleDateFormat("dd MMM, yyyy")
                val date = currentFormat.parse(this)
                val simpleDateFormat = SimpleDateFormat(ISO_DATE_FORMAT)
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
                val currentFormat = SimpleDateFormat(ISO_DATE_FORMAT)
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

    fun String.convertIntoFilterDateFormat(requiredFormat: SimpleDateFormat): String {
        return if (this.isNullOrEmpty()) ""
        else {
            try {
                val currentFormat = SimpleDateFormat("dd/MM/yyyy")
                val date = currentFormat.parse(this)
                requiredFormat.format(date)
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun String.getTimeAgo(): String {
        return if (this.isEmpty())
            ""
        else {
            try {
                val currentFormat = SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault())
                currentFormat.timeZone = TimeZone.getTimeZone("UTC")
                val time = currentFormat.parse(this).time
                val now = System.currentTimeMillis()
                val difference: Long = now - time
                val days = (difference / (1000 * 60 * 60 * 24)).toInt()
                val hours = ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60)).toInt()
                val min = ((difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60)).toInt()
                if (days != 0) {
                    if (days >= 365) {
                        val years = days / 365
                        "$years ${appendToStringIfGreaterThanOne(str = "year", value = years)} ago"
                    } else if (days >= 30) {
                        val months = days / 30
                        "$months ${appendToStringIfGreaterThanOne(str = "month", value = months)} ago"
                    } else if (days >= 7) {
                        val weeks = days / 7
                        "$weeks ${appendToStringIfGreaterThanOne(str = "week", value = weeks)} ago"
                    } else if (days == 1) {
                        "Yesterday"
                    } else {
                        "$days days ago"
                    }
                } else if (hours != 0) {
                    "$hours ${appendToStringIfGreaterThanOne(str = "hour", value = hours)} ago"
                } else if (min != 0) {
                    "$min ${appendToStringIfGreaterThanOne(str = "minute", value = min)} ago"
                } else {
                    "Just now"
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    private fun appendToStringIfGreaterThanOne(str: String, value: Int) =
        if (value > 1) "${str}s" else str

    fun String.getAge(): String {
        return if (this.isNullOrEmpty())
            "00"
        else {
            try {
                val from = LocalDate.parse(this, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT))
                val today = LocalDate.now()
                val period = Period.between(from, today)
                if (period.years == 0) {
                    "00"
                } else
                    period.years.toString()
            } catch (e: Exception) {
                "00"
            }
        }
    }

    fun String.convertFilterDateIntoISODateFormat(filterType: String, isEndDate: Boolean = false, isEndYear: Boolean = false): String {
        return try {
            val currentFormat = when (filterType) {
                FilterType.YEAR.name, FilterType.YEAR_RANGE.name -> SimpleDateFormat(FILTER_YEAR_FORMAT)
                FilterType.MONTH.name, FilterType.MONTH_RANGE.name -> SimpleDateFormat(FILTER_MONTH_FORMAT)
                else -> SimpleDateFormat(FILTER_DATE_FORMAT)
            }
            val simpleDateFormat = SimpleDateFormat(ISO_DATE_FORMAT)
            val date = currentFormat.parse(this)
            if (isEndDate) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.getActualMaximum(Calendar.DATE))
                date.time = calendar.timeInMillis
                simpleDateFormat.format(date)
            } else if (isEndYear) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(calendar.get(Calendar.YEAR), calendar.getActualMaximum(Calendar.MONTH), calendar.getActualMaximum(Calendar.DATE))
                date.time = calendar.timeInMillis
                simpleDateFormat.format(date)
            } else
                simpleDateFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}