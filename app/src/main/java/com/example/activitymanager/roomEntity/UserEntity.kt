package com.example.activitymanager.roomEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val username: String = "",
    val address: String = "",
    val phone: String = ""
)