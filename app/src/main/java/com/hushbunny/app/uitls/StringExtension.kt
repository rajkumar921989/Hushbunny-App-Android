package com.hushbunny.app.uitls

fun String?.toIntOrZero(): Int {
    return if (this.isNullOrEmpty()) 0 else {
        val value = this.toIntOrNull()
        value ?: 0
    }
}

fun String?.prependZeroToStringIfSingleDigit(): String {
    val intValue = this.toIntOrZero()
    return if (intValue > 9) intValue.toString()
    else "0${intValue}"
}

fun appendSToStringIfGreaterThanOne(str: String, value: Int) =
    if (value > 1) "${str}s" else str