package com.mdstudio.closedtesttracker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

object UsageReader {
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    private const val TOTAL_LOOKBACK_DAYS = 180L
    fun hasUsageAccess(context: Context): Boolean {
        return runCatching {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(false)
    }

    fun todayUsageMinutes(context: Context, packageName: String): Long {
        val start = startOfToday()
        return usageMinutesBetween(context, packageName, start, System.currentTimeMillis())
    }

    fun usageMinutesByDay(context: Context, packageName: String, dayStartMillis: Long): Long {
        val dayEnd = dayStartMillis + DAY_MILLIS
        return usageMinutesForDailyWindow(context, packageName, dayStartMillis, min(dayEnd, System.currentTimeMillis()))
    }

    fun usageMinutesSince(context: Context, packageName: String, startMillis: Long): Long {
        return usageMinutesBetween(context, packageName, startMillis, System.currentTimeMillis())
    }

    fun usageMinutesBetweenRange(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long
    ): Long {
        return usageMinutesBetween(context, packageName, startMillis, endMillis)
    }

    fun bestEffortTotalUsageMinutes(
        context: Context,
        packageName: String,
        seriesStartMillis: Long,
        dailyTotalMinutes: Long,
        eventTotalMinutes: Long
    ): Long {
        val now = System.currentTimeMillis()
        val seriesTotal = usageMinutesBetween(context, packageName, seriesStartMillis, now)
        val lookbackStart = max(0L, now - (TOTAL_LOOKBACK_DAYS * DAY_MILLIS))
        val lookbackTotal = usageMinutesBetween(context, packageName, lookbackStart, now)
        return maxOf(seriesTotal, lookbackTotal, dailyTotalMinutes, eventTotalMinutes)
    }

    fun usageMinutesByDayMap(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long
    ): Map<Long, Long> {
        if (endMillis <= startMillis) return emptyMap()
        val now = System.currentTimeMillis()
        val firstDayStart = startOfDay(startMillis)
        val lastReadableDayStart = startOfDay(min(endMillis, now))
        val result = linkedMapOf<Long, Long>()
        var cursor = firstDayStart
        while (cursor <= lastReadableDayStart) {
            val dayEnd = min(cursor + DAY_MILLIS, min(endMillis, now))
            if (dayEnd > cursor) {
                result[cursor] = usageMinutesBetween(context, packageName, cursor, dayEnd)
            }
            cursor += DAY_MILLIS
        }
        return result
    }

    fun mergedUsageMinutesByDayMap(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long
    ): Map<Long, Long> {
        val aggregate = usageMinutesByDayMap(context, packageName, startMillis, endMillis)
        val events = usageMinutesByDayMapFromEvents(context, packageName, startMillis, endMillis)
        if (aggregate.isEmpty()) return events
        if (events.isEmpty()) return aggregate
        return (aggregate.keys + events.keys).associateWith { dayStart ->
            maxOf(aggregate[dayStart] ?: 0L, events[dayStart] ?: 0L)
        }
    }

    fun usageMinutesByDayMapFromEvents(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long
    ): Map<Long, Long> {
        if (endMillis <= startMillis) return emptyMap()
        return runCatching {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = manager.queryEvents(startMillis, endMillis)
            val event = UsageEvents.Event()
            val totals = linkedMapOf<Long, Long>()
            var sessionStart: Long? = null

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.packageName != packageName) continue
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        if (sessionStart == null) {
                            sessionStart = event.timeStamp
                        }
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val startedAt = sessionStart ?: continue
                        addSessionToDayMap(
                            totals = totals,
                            startMillis = startedAt,
                            endMillis = event.timeStamp.coerceAtMost(endMillis)
                        )
                        sessionStart = null
                    }
                }
            }

            sessionStart?.let { startedAt ->
                addSessionToDayMap(
                    totals = totals,
                    startMillis = startedAt,
                    endMillis = endMillis
                )
            }

            totals.mapValues { (_, totalMillis) ->
                max(0L, totalMillis / 1000L / 60L)
            }
        }.getOrDefault(emptyMap())
    }

    fun todayUsageMinutesMap(context: Context, packageNames: Collection<String>): Map<String, Long> {
        if (packageNames.isEmpty()) return emptyMap()
        return usageMinutesMapBetween(context, packageNames, startOfToday(), System.currentTimeMillis())
    }

    private fun startOfToday(): Long {
        return Calendar.getInstance().apply {
            clearToDayStart()
        }.timeInMillis
    }

    private fun startOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            clearToDayStart()
        }.timeInMillis
    }

    private fun addSessionToDayMap(
        totals: MutableMap<Long, Long>,
        startMillis: Long,
        endMillis: Long
    ) {
        if (endMillis <= startMillis) return
        var cursor = startMillis
        while (cursor < endMillis) {
            val dayStart = startOfDay(cursor)
            val dayEnd = dayStart + DAY_MILLIS
            val sliceEnd = min(dayEnd, endMillis)
            totals[dayStart] = (totals[dayStart] ?: 0L) + (sliceEnd - cursor)
            cursor = sliceEnd
        }
    }

    private fun Calendar.clearToDayStart() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun usageMinutesMapBetween(
        context: Context,
        packageNames: Collection<String>,
        start: Long,
        end: Long
    ): Map<String, Long> {
        return runCatching {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = manager.queryAndAggregateUsageStats(start, end)
            packageNames.associateWith { packageName ->
                val total = stats[packageName]?.totalTimeInForeground ?: 0L
                max(0L, total / 1000L / 60L)
            }
        }.getOrDefault(packageNames.associateWith { 0L })
    }

    private fun usageMinutesBetween(
        context: Context,
        packageName: String,
        start: Long,
        end: Long
    ): Long {
        return runCatching {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = manager.queryAndAggregateUsageStats(start, end)
            val total = stats[packageName]?.totalTimeInForeground ?: 0L
            max(0L, total / 1000L / 60L)
        }.getOrDefault(0L)
    }

    private fun usageMinutesForDailyWindow(
        context: Context,
        packageName: String,
        start: Long,
        end: Long
    ): Long = usageMinutesBetween(context, packageName, start, end)
}
