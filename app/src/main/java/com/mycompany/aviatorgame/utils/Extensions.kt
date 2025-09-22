package com.mycompany.aviatorgame.utils

import java.text.DecimalFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun Float.formatMultiplier(): String {
    return DecimalFormat("#.##").format(this) + "x"
}

fun Int.formatCoins(): String {
    return when {
        this >= 1000000 -> String.format("%.1fM", this / 1000000.0)
        this >= 1000 -> String.format("%.1fK", this / 1000.0)
        else -> this.toString()
    }
}

fun Long.isToday(): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)
    val todayYear = calendar.get(Calendar.YEAR)

    calendar.timeInMillis = this
    val checkDay = calendar.get(Calendar.DAY_OF_YEAR)
    val checkYear = calendar.get(Calendar.YEAR)

    return today == checkDay && todayYear == checkYear
}

fun Long.daysSince(): Int {
    val diff = System.currentTimeMillis() - this
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}