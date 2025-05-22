package com.example.activitymanager

import android.app.Application
import com.example.activitymanager.roomEntity.QuarterStatEntity
import com.example.activitymanager.roomEntity.TypeStatEntity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).statisticDao()

    var quarterStats by mutableStateOf(emptyList<QuarterStatEntity>())
        private set

    var typeStats by mutableStateOf(emptyList<TypeStatEntity>())
        private set

    init {
        viewModelScope.launch {
            quarterStats = dao.getAllQuarterStats()
            typeStats = dao.getAllTypeStats()
        }
    }
}
