package com.transition.lensesdelivery.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'"
fun getDateTime(): Date? {
    val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    val formattedDateTime = dateFormat.format(Calendar.getInstance().time)
    return stringToTime(formattedDateTime)
}

fun getDateTimeStr(): String {
    val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return dateFormat.format(Calendar.getInstance().time)
}

fun timeToString(time: Date?): String? {
    val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return time?.let { dateFormat.format(it) }
}

fun stringToTime(string: String?): Date? {
    val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    var parsedDate: Date? = null
    parsedDate = string?.let { dateFormat.parse(it) }
    return parsedDate
}