package com.example.testerapp

import com.example.testerapp.data.TrackedApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.min

object SeriesCalculator {
    fun currentDay(item: TrackedApp, now: Long = System.currentTimeMillis()): Int {
        val diff = (now - item.createdAtMillis).coerceAtLeast(0L)
        val passedDays = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        return min(14, item.startDayIndex + passedDays)
    }

    fun dayStartMillis(offsetFromToday: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, offsetFromToday)
        }.timeInMillis
    }
}
