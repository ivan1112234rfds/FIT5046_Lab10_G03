package com.example.activitymanager.roomEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "type_stats")
data class TypeStatEntity(
    @PrimaryKey val type: String,  // Hiking, Movie 等
    val count: Int
)