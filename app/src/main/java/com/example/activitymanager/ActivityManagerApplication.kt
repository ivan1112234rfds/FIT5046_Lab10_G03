package com.example.activitymanager

import android.app.Application
import com.example.activitymanager.firebase.AuthManager

class ActivityManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化AuthManager
        AuthManager.initialize()
    }
} 