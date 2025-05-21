package com.example.activitymanager

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 创建定时任务请求
        val workRequest = PeriodicWorkRequestBuilder<StatisticWorker>(
            15, TimeUnit.MINUTES
        ).build()

        // 启动定时任务
        WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<StatisticWorker>().build())
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StatisticWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
