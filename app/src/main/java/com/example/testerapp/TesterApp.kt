package com.example.testerapp

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TesterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_series_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
