package com.example.testerapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.testerapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClosedTestWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, manager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ClosedTestWidgetProvider::class.java))
            updateWidgets(context, manager, ids)
        }

        private fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                val items = AppDatabase.get(context).appDao().observeAll().first().filterNot { it.isArchived }
                val active = items.count { SeriesCalculator.currentDay(it) < 14 && it.completedAtMillis == null }
                val completed = items.count { SeriesCalculator.currentDay(it) >= 14 || it.completedAtMillis != null }
                val missing = if (UsageReader.hasUsageAccess(context)) {
                    items.count {
                        SeriesCalculator.currentDay(it) < 14 &&
                            it.completedAtMillis == null &&
                            UsageReader.todayUsageMinutes(context, it.packageName) == 0L
                    }
                } else {
                    active
                }
                val title = if (missing > 0) "$missing eksik test" else "Seri tamam"
                val subtitle = "$active aktif • $completed tamam"

                ids.forEach { id ->
                    manager.updateAppWidget(id, buildViews(context, title, subtitle))
                }
            }
        }

        private fun buildViews(context: Context, title: String, subtitle: String): RemoteViews {
            val intent = Intent(context, MainActivity::class.java)
            val flags = if (Build.VERSION.SDK_INT >= 23) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
            return RemoteViews(context.packageName, R.layout.widget_series_status).apply {
                setTextViewText(R.id.widgetTitle, title)
                setTextViewText(R.id.widgetSubtitle, subtitle)
                setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            }
        }
    }
}
