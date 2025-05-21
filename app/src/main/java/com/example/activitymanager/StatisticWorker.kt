package com.example.activitymanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log

import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.roomEntity.QuarterStatEntity
import com.example.activitymanager.roomEntity.TypeStatEntity

class StatisticWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        Log.d("WorkManager", "Running scheduled task")

        val helper = FirebaseHelper()
        val quarterCounts = helper.getActivityCountByQuarter()
        val typeCounts = helper.getActivityCountByType()

        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.statisticDao()

        val quarterEntities = quarterCounts.map { (q, c) ->
            QuarterStatEntity(quarter = q, count = c)
        }
        val typeEntities = typeCounts.map { (t, c) ->
            TypeStatEntity(type = t, count = c)
        }

        dao.insertAllQuarterStats(quarterEntities)
        dao.insertAllTypeStats(typeEntities)

        return Result.success()
    }
}