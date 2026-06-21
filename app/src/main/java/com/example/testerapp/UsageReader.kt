package com.mdstudio.closedtesttracker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

object UsageReader {
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    private const val TOTAL_LOOKBACK_DAYS = 180L
    private const val PREFS_NAME = "tester_settings"
    private const val KEY_MANUAL_USAGE_PREFIX = "manual_usage_"
    private const val KEY_PENDING_LAUNCH_PACKAGE = "pending_launch_package"
    private const val KEY_PENDING_LAUNCH_STARTED_AT = "pending_launch_started_at"
    private const val MIN_MANUAL_SESSION_MILLIS = 10L * 1000L
    private const val MAX_MANUAL_SESSION_MILLIS = 8L * 60L * 60L * 1000L
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
        val end = System.currentTimeMillis()
        val eventMinutes = usageMinutesByDayMapFromEventsForDisplay(context, packageName, start, end).values.sum()
        val detected = if (eventMinutes > 0L) eventMinutes else webBackedFallbackMinutes(context, packageName, start, end)
        val manual = manualUsageMinutesByDayMap(context, packageName, start, end).values.sum()
        return maxOf(detected, manual)
    }

    fun markAppLaunched(context: Context, packageName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_LAUNCH_PACKAGE, packageName)
            .putLong(KEY_PENDING_LAUNCH_STARTED_AT, System.currentTimeMillis())
            .apply()
    }

    fun finishPendingLaunchedSession(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val packageName = prefs.getString(KEY_PENDING_LAUNCH_PACKAGE, null)?.takeIf { it.isNotBlank() }
        val startedAt = prefs.getLong(KEY_PENDING_LAUNCH_STARTED_AT, 0L)
        prefs.edit()
            .remove(KEY_PENDING_LAUNCH_PACKAGE)
            .remove(KEY_PENDING_LAUNCH_STARTED_AT)
            .apply()
        if (packageName == null || startedAt <= 0L) return false
        val endedAt = System.currentTimeMillis()
        val duration = endedAt - startedAt
        if (duration !in MIN_MANUAL_SESSION_MILLIS..MAX_MANUAL_SESSION_MILLIS) return false
        addManualSession(context, packageName, startedAt, endedAt)
        return true
    }

    fun usageMinutesByDay(context: Context, packageName: String, dayStartMillis: Long): Long {
        val dayEnd = dayStartMillis + DAY_MILLIS
        return usageMinutesForDailyWindow(context, packageName, dayStartMillis, min(dayEnd, System.currentTimeMillis()))
    }

    fun usageMinutesSince(context: Context, packageName: String, startMillis: Long): Long {
        return bestUsageMinutesBetween(context, packageName, startMillis, System.currentTimeMillis())
    }

    fun usageMinutesBetweenRange(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long
    ): Long {
        return bestUsageMinutesBetween(context, packageName, startMillis, endMillis)
    }

    fun bestEffortTotalUsageMinutes(
        context: Context,
        packageName: String,
        seriesStartMillis: Long,
        dailyTotalMinutes: Long,
        eventTotalMinutes: Long
    ): Long {
        val now = System.currentTimeMillis()
        val seriesTotal = bestUsageMinutesBetween(
            context = context,
            packageName = packageName,
            start = seriesStartMillis,
            end = now,
            requireRelatedEventForAggregate = true
        )
        val lookbackStart = max(0L, now - (TOTAL_LOOKBACK_DAYS * DAY_MILLIS))
        val lookbackTotal = bestUsageMinutesBetween(
            context = context,
            packageName = packageName,
            start = lookbackStart,
            end = now,
            requireRelatedEventForAggregate = true
        )
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
                result[cursor] = bestUsageMinutesBetween(
                    context = context,
                    packageName = packageName,
                    start = cursor,
                    end = dayEnd,
                    requireRelatedEventForAggregate = true
                )
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
        val events = usageMinutesByDayMapFromEvents(
            context = context,
            packageName = packageName,
            startMillis = startMillis,
            endMillis = endMillis,
            includeRelatedPackage = isLikelyWebBackedApp(context, packageName)
        )
        val manual = manualUsageMinutesByDayMap(context, packageName, startMillis, endMillis)
        if (aggregate.isEmpty() && events.isEmpty()) return manual
        return (aggregate.keys + events.keys + manual.keys).associateWith { dayStart ->
            maxOf(aggregate[dayStart] ?: 0L, events[dayStart] ?: 0L, manual[dayStart] ?: 0L)
        }
    }

    fun usageMinutesByDayMapFromEvents(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long,
        includeRelatedPackage: Boolean = false
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
                val belongs = if (includeRelatedPackage) {
                    event.isRelatedToPackage(packageName)
                } else {
                    event.belongsToPackage(packageName)
                }
                if (!belongs) continue
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

    fun usageMinutesByDayMapFromEventsForDisplay(
        context: Context,
        packageName: String,
        startMillis: Long,
        endMillis: Long
    ): Map<Long, Long> {
        return usageMinutesByDayMapFromEvents(
            context = context,
            packageName = packageName,
            startMillis = startMillis,
            endMillis = endMillis,
            includeRelatedPackage = isLikelyWebBackedApp(context, packageName)
        )
    }

    fun todayUsageMinutesMap(context: Context, packageNames: Collection<String>): Map<String, Long> {
        if (packageNames.isEmpty()) return emptyMap()
        val start = startOfToday()
        val end = System.currentTimeMillis()
        return packageNames.associateWith { packageName ->
            val eventMinutes = usageMinutesByDayMapFromEvents(
                context = context,
                packageName = packageName,
                startMillis = start,
                endMillis = end,
                includeRelatedPackage = isLikelyWebBackedApp(context, packageName)
            ).values.sum()
            val detected = if (eventMinutes > 0L) eventMinutes else webBackedFallbackMinutes(context, packageName, start, end)
            val manual = manualUsageMinutesByDayMap(context, packageName, start, end).values.sum()
            maxOf(detected, manual)
        }
    }

    fun totalUsageMinutesMap(context: Context, packageNames: Collection<String>): Map<String, Long> {
        if (packageNames.isEmpty()) return emptyMap()
        val now = System.currentTimeMillis()
        val lookbackStart = max(0L, now - (TOTAL_LOOKBACK_DAYS * DAY_MILLIS))
        return usageMinutesMapBetween(
            context = context,
            packageNames = packageNames,
            start = lookbackStart,
            end = now,
            includeTaskRootEvents = true
        )
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
        end: Long,
        includeTaskRootEvents: Boolean = false,
        requireRelatedEventForAggregate: Boolean = false
    ): Map<String, Long> {
        return runCatching {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = manager.queryAndAggregateUsageStats(start, end)
            packageNames.associateWith { packageName ->
                val total = stats[packageName]?.totalTimeInForeground ?: 0L
                val aggregateMinutes = max(0L, total / 1000L / 60L)
                if (includeTaskRootEvents) {
                    val eventMinutes = usageMinutesByDayMapFromEventsForDisplay(
                        context = context,
                        packageName = packageName,
                        startMillis = start,
                        endMillis = end
                    ).values.sum()
                    val hasUsageEvent = eventMinutes > 0L || hasRelatedUsageEvent(context, packageName, start, end)
                    val safeAggregate = if (!requireRelatedEventForAggregate || hasUsageEvent) aggregateMinutes else 0L
                    val direct = maxOf(
                        safeAggregate,
                        eventMinutes
                    )
                    val detected = if (direct > 0L) direct else webBackedFallbackMinutes(context, packageName, start, end)
                    val manual = manualUsageMinutesByDayMap(context, packageName, start, end).values.sum()
                    maxOf(detected, manual)
                } else {
                    val hasUsageEvent = !requireRelatedEventForAggregate || hasRelatedUsageEvent(context, packageName, start, end)
                    val detected = if (aggregateMinutes > 0L && hasUsageEvent) aggregateMinutes else webBackedFallbackMinutes(context, packageName, start, end)
                    val manual = manualUsageMinutesByDayMap(context, packageName, start, end).values.sum()
                    maxOf(detected, manual)
                }
            }
        }.getOrDefault(packageNames.associateWith { 0L })
    }

    private fun bestUsageMinutesBetween(
        context: Context,
        packageName: String,
        start: Long,
        end: Long,
        requireRelatedEventForAggregate: Boolean = false
    ): Long {
        val aggregate = usageMinutesBetween(context, packageName, start, end)
        val events = usageMinutesByDayMapFromEventsForDisplay(
            context = context,
            packageName = packageName,
            startMillis = start,
            endMillis = end
        ).values.sum()
        val hasUsageEvent = events > 0L || hasRelatedUsageEvent(context, packageName, start, end)
        val safeAggregate = if (!requireRelatedEventForAggregate || hasUsageEvent) aggregate else 0L
        val direct = maxOf(safeAggregate, events)
        val detected = if (direct > 0L) direct else webBackedFallbackMinutes(context, packageName, start, end)
        val manual = manualUsageMinutesByDayMap(context, packageName, start, end).values.sum()
        return maxOf(detected, manual)
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
    ): Long = bestUsageMinutesBetween(context, packageName, start, end)

    private fun addManualSession(
        context: Context,
        packageName: String,
        start: Long,
        end: Long
    ) {
        val totals = linkedMapOf<Long, Long>()
        addSessionToDayMap(totals, start, end)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        totals.forEach { (dayStart, millis) ->
            val key = manualUsageKey(packageName, dayStart)
            editor.putLong(key, prefs.getLong(key, 0L) + millis)
        }
        editor.apply()
    }

    private fun manualUsageMinutesByDayMap(
        context: Context,
        packageName: String,
        start: Long,
        end: Long
    ): Map<Long, Long> {
        if (end <= start) return emptyMap()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val result = linkedMapOf<Long, Long>()
        var cursor = startOfDay(start)
        val lastDay = startOfDay(end)
        while (cursor <= lastDay) {
            val millis = prefs.getLong(manualUsageKey(packageName, cursor), 0L)
            if (millis > 0L) {
                result[cursor] = max(1L, (millis + 59_999L) / 60_000L)
            }
            cursor += DAY_MILLIS
        }
        return result
    }

    private fun manualUsageKey(packageName: String, dayStart: Long): String {
        return "$KEY_MANUAL_USAGE_PREFIX${packageName}_$dayStart"
    }

    private fun hasRelatedUsageEvent(
        context: Context,
        packageName: String,
        start: Long,
        end: Long
    ): Boolean {
        if (end <= start) return false
        val includeRelatedPackage = isLikelyWebBackedApp(context, packageName)
        return runCatching {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = manager.queryEvents(start, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val belongs = if (includeRelatedPackage) {
                    event.isRelatedToPackage(packageName)
                } else {
                    event.belongsToPackage(packageName)
                }
                if (belongs && event.isForegroundUsageEvent()) return true
            }
            false
        }.getOrDefault(false)
    }

    private fun webBackedFallbackMinutes(
        context: Context,
        packageName: String,
        start: Long,
        end: Long
    ): Long {
        if (!isLikelyWebBackedApp(context, packageName)) return 0L
        return usageMinutesByDayMapFromEvents(
            context = context,
            packageName = packageName,
            startMillis = start,
            endMillis = end,
            includeRelatedPackage = true
        ).values.sum()
    }

    private fun isLikelyWebBackedApp(context: Context, packageName: String): Boolean {
        if (packageName.startsWith("org.chromium.webapk.")) return true
        if (packageName.contains("webapk", ignoreCase = true) || packageName.contains("pwa", ignoreCase = true)) return true
        return runCatching {
            val info = context.packageManager.getPackageInfoCompat(
                packageName,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
            )
            val componentNames = buildList {
                info.activities?.forEach { add(it.name) }
                info.services?.forEach { add(it.name) }
            }
            componentNames.any { name ->
                name.contains("webapk", ignoreCase = true) ||
                    name.contains("trusted", ignoreCase = true) ||
                    name.contains("browserhelper", ignoreCase = true) ||
                    name.contains("customtabs", ignoreCase = true)
            }
        }.getOrDefault(false)
    }

    private fun PackageManager.getPackageInfoCompat(
        packageName: String,
        flags: Int = 0
    ) = if (Build.VERSION.SDK_INT >= 33) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, flags)
    }

    private fun UsageEvents.Event.belongsToPackage(packageName: String): Boolean {
        return this.packageName == packageName
    }

    private fun UsageEvents.Event.isRelatedToPackage(packageName: String): Boolean {
        if (belongsToPackage(packageName)) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val taskRootPackage = runCatching {
                javaClass.getMethod("getTaskRootPackageName").invoke(this) as? String
            }.getOrNull()
            if (taskRootPackage == packageName) return true
        }
        return false
    }

    private fun UsageEvents.Event.isForegroundUsageEvent(): Boolean {
        return when (eventType) {
            UsageEvents.Event.ACTIVITY_RESUMED,
            UsageEvents.Event.ACTIVITY_PAUSED,
            UsageEvents.Event.ACTIVITY_STOPPED,
            UsageEvents.Event.MOVE_TO_FOREGROUND,
            UsageEvents.Event.MOVE_TO_BACKGROUND -> true
            else -> false
        }
    }
}
