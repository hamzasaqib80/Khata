package com.khata.app.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Utility for date manipulation and formatting.
 */
object DateUtils {

    private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM")

    fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun LocalDate.toEpochMs(): Long {
        return this.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /** Example: "Monday, 14 June 2026" */
    fun LocalDate.formatDisplay(): String = this.format(displayFormatter)

    /** Example: "14 Jun" */
    fun LocalDate.formatShort(): String = this.format(shortFormatter)

    /** Example: "June 2026" */
    fun monthYearDisplay(month: Int, year: Int): String {
        val monthName = LocalDate.of(year, month, 1)
            .month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        return "$monthName $year"
    }

    /** Returns all dates in a specific month */
    fun daysInMonth(month: Int, year: Int): List<LocalDate> {
        val firstDay = LocalDate.of(year, month, 1)
        val length = firstDay.lengthOfMonth()
        return (0 until length).map { firstDay.plusDays(it.toLong()) }
    }
}
