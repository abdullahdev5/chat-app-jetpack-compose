package com.android.chatappcompose.core.domain.common_function

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatTimestampToAmPm(timestamp: Timestamp?): String {
    val date = timestamp?.toDate()
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Use "a" for AM/PM
    return dateFormat.format(date)
}

fun formatTimestampToDate(timestamp: Timestamp?): String {
    val date = timestamp?.toDate()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

fun formatTimeStampWithMonthName(
    timestamp: Timestamp?,
    monthName: String
): String {
    val timestampCalendar = Calendar
        .getInstance()
        .apply {
            time = timestamp?.toDate()
        }

    val day = timestampCalendar.get(Calendar.DAY_OF_MONTH)
    val year = timestampCalendar.get(Calendar.YEAR)

    return "$day $monthName $year"
}