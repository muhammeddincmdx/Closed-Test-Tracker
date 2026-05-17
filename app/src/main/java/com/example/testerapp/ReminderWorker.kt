package com.mdstudio.closedtesttracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mdstudio.closedtesttracker.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.Locale

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val activeItems = AppDatabase.get(applicationContext).appDao().observeAll().first()
            .filter { !it.isArchived && it.completedAtMillis == null }
        if (activeItems.isEmpty()) return Result.success()
        val language = resolveNotificationLanguage()

        if (!UsageReader.hasUsageAccess(applicationContext)) {
            showNotification(
                t(
                    language,
                    "Kullanim izni gerekli",
                    "Usage access required",
                    "Acces a l'utilisation requis",
                    "Se requiere acceso de uso",
                    "需要使用情况访问权限",
                    "Usage access ki zarurat hai",
                    "Требуется доступ к статистике использования"
                ),
                t(
                    language,
                    "Seri hatirlatmalari icin kullanim erisimi iznini ac.",
                    "Enable usage access for streak reminders.",
                    "Activez l'acces a l'utilisation pour les rappels de serie.",
                    "Activa el acceso de uso para los recordatorios de racha.",
                    "请开启使用情况访问权限以接收连续提醒。",
                    "Streak reminder ke liye usage access on karein.",
                    "Включите доступ к статистике использования для напоминаний о серии."
                )
            )
            return Result.success()
        }

        val missingToday = activeItems.filter {
            UsageReader.todayUsageMinutes(applicationContext, it.packageName) == 0L
        }
        if (missingToday.isNotEmpty()) {
            showNotification(
                t(
                    language,
                    "Gunluk seri gidiyor",
                    "Daily streak in progress",
                    "Serie quotidienne en cours",
                    "Racha diaria en curso",
                    "今日连续记录进行中",
                    "Daily streak chal rahi hai",
                    "Ежедневная серия продолжается"
                ),
                t(
                    language,
                    "${missingToday.size} test uygulamasi bugun henuz acilmadi.",
                    "${missingToday.size} test apps have not been opened today yet.",
                    "${missingToday.size} applis de test n'ont pas encore ete ouvertes aujourd'hui.",
                    "${missingToday.size} apps de prueba aun no se han abierto hoy.",
                    "${missingToday.size} 个测试应用今天还没有打开。",
                    "${missingToday.size} test apps aaj abhi tak nahi khuli hain.",
                    "${missingToday.size} тестовых приложений сегодня еще не открывались."
                )
            )
            return Result.success()
        }

        val readyToFinish = activeItems.filter { SeriesCalculator.currentDay(it) >= 14 }
        if (readyToFinish.isNotEmpty()) {
            showNotification(
                t(
                    language,
                    "Test tamamlanmayi bekliyor",
                    "Test waiting to be finished",
                    "Test en attente de finalisation",
                    "La prueba espera finalizarse",
                    "测试等待完成",
                    "Test finish hone ka intezar kar raha hai",
                    "Тест ожидает завершения"
                ),
                t(
                    language,
                    "${readyToFinish.size} uygulama 14 gunu gecti. Bitir demeden seri saymaya devam eder.",
                    "${readyToFinish.size} apps passed day 14. The streak keeps counting until you finish it.",
                    "${readyToFinish.size} applis ont depasse le jour 14. La serie continue tant que vous ne la terminez pas.",
                    "${readyToFinish.size} apps superaron el dia 14. La racha sigue hasta que la finalices.",
                    "${readyToFinish.size} 个应用已超过第 14 天。在你手动完成前会继续累计。",
                    "${readyToFinish.size} apps day 14 paar kar chuki hain. Jab tak finish nahi karte streak chalti rahegi.",
                    "${readyToFinish.size} приложений прошли 14-й день. Серия продолжается, пока вы не завершите её."
                )
            )
        }
        return Result.success()
    }

    private fun resolveNotificationLanguage(): String {
        val raw = applicationContext
            .getSharedPreferences("tester_settings", Context.MODE_PRIVATE)
            .getString("language", "SYSTEM")
            ?.uppercase(Locale.ROOT)
            ?: "SYSTEM"

        return when (raw) {
            "TR", "EN", "FR", "ES", "ZH", "HI", "RU" -> raw
            else -> when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
                "tr" -> "TR"
                "fr" -> "FR"
                "es" -> "ES"
                "zh" -> "ZH"
                "hi" -> "HI"
                "ru" -> "RU"
                else -> "EN"
            }
        }
    }

    private fun t(
        language: String,
        tr: String,
        en: String,
        fr: String = en,
        es: String = en,
        zh: String = en,
        hi: String = en,
        ru: String = en
    ): String {
        return when (language) {
            "TR" -> tr
            "FR" -> fr
            "ES" -> es
            "ZH" -> zh
            "HI" -> hi
            "RU" -> ru
            else -> en
        }
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
                NotificationChannel(channelId, "Series reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        val contentIntent = PendingIntent.getActivity(applicationContext, 1001, intent, pendingFlags)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }
}
