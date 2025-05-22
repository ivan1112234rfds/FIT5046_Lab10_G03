package com.example.activitymanager

import android.app.Application
import com.example.activitymanager.firebase.AuthManager
import androidx.work.*
import java.util.concurrent.TimeUnit

class ActivityManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AuthManager
        AuthManager.initialize()

        val workRequest = PeriodicWorkRequestBuilder<StatisticWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<StatisticWorker>().build())
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StatisticWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
} 