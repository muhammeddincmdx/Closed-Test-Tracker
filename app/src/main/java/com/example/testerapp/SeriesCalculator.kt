package com.example.testerapp

import com.example.testerapp.data.TrackedApp
import java.util.Calendar

object SeriesCalculator {
    fun currentDay(item: TrackedApp, now: Long = System.currentTimeMillis()): Int {
        val effectiveNow = item.completedAtMillis?.coerceAtMost(now) ?: now
        val createdDayStart = startOfDayMillis(item.createdAtMillis)
        val nowDayStart = startOfDayMillis(effectiveNow)

        val passedDays =
            ((nowDayStart - createdDayStart) / (24L * 60L * 60L * 1000L))
                .toInt()
                .coerceAtLeast(0)

        return item.startDayIndex + passedDays
    }

    private fun startOfDayMillis(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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
