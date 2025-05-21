package com.example.activitymanager.roomEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quarter_stats")
data class QuarterStatEntity(
    @PrimaryKey val quarter: String,  // Q1、Q2、Q3、Q4
    val count: Int
)