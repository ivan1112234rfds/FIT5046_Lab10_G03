package com.example.activitymanager.mapper

import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class Activity(
    val id: String,
    val title: String,
    val description: String,
    val date: Date,
    val location: String,
    val organizer: String,
    val rating: Double,
    val duration: String,
    val type: String,
    val participants: Int,
    val isFavorite: Boolean = false,
    val coordinates: LatLng = LatLng(0.0, 0.0)
)