package com.example.activitymanager.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.activitymanager.roomEntity.QuarterStatEntity
import com.example.activitymanager.roomEntity.TypeStatEntity

@Dao
interface StatisticDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuarterStats(stats: List<QuarterStatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTypeStats(stats: List<TypeStatEntity>)

    @Query("SELECT * FROM quarter_stats")
    suspend fun getAllQuarterStats(): List<QuarterStatEntity>

    @Query("SELECT * FROM type_stats")
    suspend fun getAllTypeStats(): List<TypeStatEntity>
}
