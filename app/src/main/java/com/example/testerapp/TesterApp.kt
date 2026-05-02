package com.example.testerapp

import android.app.Application

class TesterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.schedule(this)
    }
}
