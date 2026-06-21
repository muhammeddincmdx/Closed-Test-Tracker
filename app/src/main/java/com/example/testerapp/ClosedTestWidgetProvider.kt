package com.mdstudio.closedtesttracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mdstudio.closedtesttracker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClosedTestWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_OPEN_TEST_APP) {
            val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
            context.packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
                UsageReader.markAppLaunched(context, packageName)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }

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
                val active = items.count { it.completedAtMillis == null }
                val missing = if (UsageReader.hasUsageAccess(context)) {
                    items.count {
                        it.completedAtMillis == null &&
                            UsageReader.todayUsageMinutes(context, it.packageName) == 0L
                    }
                } else {
                    active
                }
                val title = if (missing > 0) {
                    context.getString(R.string.widget_missing_tests, missing)
                } else {
                    context.getString(R.string.widget_streak_complete)
                }
                ids.forEach { id ->
                    manager.updateAppWidget(id, buildViews(context, id, title))
                    manager.notifyAppWidgetViewDataChanged(id, R.id.widgetAppGrid)
                }
            }
        }

        const val ACTION_OPEN_TEST_APP = "com.mdstudio.closedtesttracker.OPEN_TEST_APP"
        const val EXTRA_PACKAGE_NAME = "package_name"

        private fun buildViews(context: Context, widgetId: Int, title: String): RemoteViews {
            val intent = Intent(context, MainActivity::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
            val serviceIntent = Intent(context, WidgetAppGridService::class.java).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            val templateIntent = Intent(context, ClosedTestWidgetProvider::class.java).setAction(ACTION_OPEN_TEST_APP)
            val template = PendingIntent.getBroadcast(
                context,
                widgetId,
                templateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            return RemoteViews(context.packageName, R.layout.widget_series_status).apply {
                setTextViewText(R.id.widgetTitle, title)
                setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
                setRemoteAdapter(R.id.widgetAppGrid, serviceIntent)
                setPendingIntentTemplate(R.id.widgetAppGrid, template)
            }
        }
    }
}
