package com.example.activitymanager

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.activitymanager.dao.UserDao
import com.example.activitymanager.dao.StatisticDao
import com.example.activitymanager.roomEntity.UserEntity
import com.example.activitymanager.roomEntity.QuarterStatEntity
import com.example.activitymanager.roomEntity.TypeStatEntity
import android.content.Context

@Database(
    entities = [UserEntity::class, QuarterStatEntity::class, TypeStatEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun statisticDao(): StatisticDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_manager.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
