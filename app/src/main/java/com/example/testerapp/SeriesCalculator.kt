package com.mdstudio.closedtesttracker

import com.mdstudio.closedtesttracker.data.TrackedApp
import java.util.Calendar
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object SeriesCalculator {
    fun currentDay(item: TrackedApp, now: Long = System.currentTimeMillis()): Int {
        val effectiveNow = item.completedAtMillis?.coerceAtMost(now) ?: now
        val zone = ZoneId.systemDefault()
        val createdDate = Instant.ofEpochMilli(item.createdAtMillis).atZone(zone).toLocalDate()
        val nowDate = Instant.ofEpochMilli(effectiveNow).atZone(zone).toLocalDate()
        val passedDays = ChronoUnit.DAYS.between(createdDate, nowDate).toInt().coerceAtLeast(0)

        return item.startDayIndex + passedDays
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

    fun dayStartMillisForTestDay(item: TrackedApp, testDay: Int): Long {
        val zone = ZoneId.systemDefault()
        val createdDate = Instant.ofEpochMilli(item.createdAtMillis).atZone(zone).toLocalDate()
        val targetDate = createdDate.plusDays((testDay - item.startDayIndex).toLong())
        return targetDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}

