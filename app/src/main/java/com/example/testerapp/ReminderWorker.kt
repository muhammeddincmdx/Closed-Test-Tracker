package com.mdstudio.closedtesttracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mdstudio.closedtesttracker.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.Locale

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val activeItems = AppDatabase.get(applicationContext).appDao().observeAll().first()
                .filter { !it.isArchived && it.completedAtMillis == null }
            if (activeItems.isEmpty()) return Result.success()

            val language = resolveNotificationLanguage()
            if (!UsageReader.hasUsageAccess(applicationContext)) {
                showNotification(copy(language, "permission_title"), copy(language, "permission_body"))
                return Result.success()
            }

            val missingToday = activeItems.filter {
                UsageReader.todayUsageMinutes(applicationContext, it.packageName) == 0L
            }
            if (missingToday.isNotEmpty()) {
                showNotification(
                    copy(language, "missing_title"),
                    copy(language, "missing_body", missingToday.size)
                )
                return Result.success()
            }

            val readyToFinish = activeItems.filter { SeriesCalculator.currentDay(it) >= 14 }
            if (readyToFinish.isNotEmpty()) {
                showNotification(
                    copy(language, "finish_title"),
                    copy(language, "finish_body", readyToFinish.size)
                )
            }
            return Result.success()
        } finally {
            ReminderScheduler.schedule(applicationContext)
        }
    }

    private fun resolveNotificationLanguage(): AppLanguage {
        val raw = applicationContext.getSharedPreferences("tester_settings", Context.MODE_PRIVATE)
            .getString("language", "SYSTEM")?.uppercase(Locale.ROOT) ?: "SYSTEM"
        runCatching { AppLanguage.valueOf(raw) }.getOrNull()?.let { return it }
        return when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
            "tr" -> AppLanguage.TR
            "fr" -> AppLanguage.FR
            "es" -> AppLanguage.ES
            "zh" -> AppLanguage.ZH
            "hi" -> AppLanguage.HI
            "ru" -> AppLanguage.RU
            "ar" -> AppLanguage.AR
            "de" -> AppLanguage.DE
            "ja" -> AppLanguage.JA
            "pt" -> AppLanguage.PT
            "in", "id" -> AppLanguage.ID
            else -> AppLanguage.EN
        }
    }

    private fun copy(language: AppLanguage, key: String, count: Int = 0): String {
        val values = when (key) {
            "permission_title" -> listOf("Kullanım izni gerekli", "Usage access required", "Accès à l'utilisation requis", "Se requiere acceso de uso", "需要使用情况访问权限", "उपयोग एक्सेस आवश्यक है", "Требуется доступ к использованию", "مطلوب الوصول إلى الاستخدام", "Nutzungszugriff erforderlich", "使用状況へのアクセスが必要です", "Acesso ao uso necessário", "Akses penggunaan diperlukan")
            "permission_body" -> listOf("Seri hatırlatmaları için kullanım erişimini aç.", "Enable Usage Access for streak reminders.", "Activez l'accès à l'utilisation pour les rappels de série.", "Activa el acceso de uso para los recordatorios de racha.", "请启用使用情况访问权限以接收连续测试提醒。", "स्ट्रीक रिमाइंडर के लिए उपयोग एक्सेस चालू करें।", "Разрешите доступ к использованию для напоминаний о серии.", "فعّل الوصول إلى الاستخدام لتذكيرات السلسلة.", "Aktiviere den Nutzungszugriff für Serienerinnerungen.", "継続リマインダーのために使用状況へのアクセスを許可してください。", "Ative o acesso ao uso para lembretes da sequência.", "Aktifkan akses penggunaan untuk pengingat rangkaian.")
            "missing_title" -> listOf("Günlük seri devam ediyor", "Daily streak in progress", "Série quotidienne en cours", "Racha diaria en curso", "每日连续测试进行中", "दैनिक स्ट्रीक जारी है", "Ежедневная серия продолжается", "السلسلة اليومية مستمرة", "Tägliche Serie läuft", "毎日の継続テストが進行中", "Sequência diária em andamento", "Rangkaian harian berlangsung")
            "missing_body" -> listOf("$count test uygulaması bugün henüz açılmadı.", "$count test apps have not been opened today yet.", "$count applications de test n'ont pas encore été ouvertes aujourd'hui.", "$count apps de prueba aún no se han abierto hoy.", "今天还有 $count 个测试应用尚未打开。", "आज $count टेस्ट ऐप अभी तक नहीं खोले गए हैं।", "Сегодня ещё не открыто тестовых приложений: $count.", "لم يتم فتح $count من تطبيقات الاختبار اليوم بعد.", "$count Test-Apps wurden heute noch nicht geöffnet.", "今日はまだ $count 個のテストアプリが開かれていません。", "$count apps de teste ainda não foram abertos hoje.", "$count aplikasi uji belum dibuka hari ini.")
            "finish_title" -> listOf("Test tamamlanmayı bekliyor", "Test waiting to be finished", "Test en attente de finalisation", "Prueba pendiente de finalizar", "测试等待完成", "टेस्ट पूरा किए जाने की प्रतीक्षा में है", "Тест ожидает завершения", "الاختبار بانتظار الإنهاء", "Test wartet auf Abschluss", "テストの完了待ちです", "Teste aguardando conclusão", "Tes menunggu diselesaikan")
            else -> listOf("$count uygulama 14 günü geçti. Bitir demeden seri saymaya devam eder.", "$count apps passed day 14. The streak keeps counting until you finish it.", "$count applications ont dépassé le jour 14. La série continue jusqu'à leur finalisation.", "$count apps superaron el día 14. La racha continúa hasta que las finalices.", "$count 个应用已超过第 14 天。在完成测试前，连续天数会继续计算。", "$count ऐप 14 दिन पार कर चुके हैं। समाप्त करने तक स्ट्रीक जारी रहेगी।", "$count приложений прошли 14-й день. Серия продолжится до завершения.", "تجاوز $count من التطبيقات اليوم 14. ستستمر السلسلة حتى تنهيها.", "$count Apps haben Tag 14 überschritten. Die Serie läuft bis zum Abschluss weiter.", "$count 個のアプリが14日目を超えました。完了するまで日数は増え続けます。", "$count apps passaram do 14º dia. A sequência continua até você finalizar.", "$count aplikasi melewati hari ke-14. Rangkaian berlanjut sampai diselesaikan.")
        }
        return values[language.ordinal]
    }

    private fun showNotification(title: String, text: String) {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "series_reminders"
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Series reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
