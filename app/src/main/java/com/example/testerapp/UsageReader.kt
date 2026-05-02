package com.example.testerapp

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar
import kotlin.math.max

object UsageReader {
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
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return usageMinutesBetween(context, packageName, start, System.currentTimeMillis())
    }

    fun usageMinutesByDay(context: Context, packageName: String, dayStartMillis: Long): Long {
        val dayEnd = dayStartMillis + 24L * 60L * 60L * 1000L
        return usageMinutesBetween(context, packageName, dayStartMillis, dayEnd)
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
}
