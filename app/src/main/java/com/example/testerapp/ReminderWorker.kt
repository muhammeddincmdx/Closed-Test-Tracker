package com.example.testerapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.testerapp.data.AppDatabase
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val items = AppDatabase.get(applicationContext).appDao().observeAll().first()
            .filterNot { it.isArchived }
        if (items.isEmpty() || !UsageReader.hasUsageAccess(applicationContext)) return Result.success()

        val hasIncomplete = items.any { item ->
            val today = UsageReader.todayUsageMinutes(applicationContext, item.packageName)
            val day = SeriesCalculator.currentDay(item)
            item.completedAtMillis == null && day >= 14 && today == 0L
        }
        if (hasIncomplete) {
            val autoTour = applicationContext
                .getSharedPreferences("tester_settings", Context.MODE_PRIVATE)
                .getBoolean("auto_tour", false)
            if (autoTour) {
                showNotification("Test serisi devam ediyor", "14. günü geçen ama tamamlanmayan uygulamalar var. Bugün kullanılmayanları kontrol et.")
            } else {
                showNotification("Test serisi uyarısı", "14. günü geçen ama tamamlanmayan test uygulaması var.")
            }
        }
        return Result.success()
    }

    private fun showNotification(title: String, text: String) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "series_reminders"
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Seri Bildirimleri", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }
}
