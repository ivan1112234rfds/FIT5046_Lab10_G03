package com.example.activitymanager.firebase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.activitymanager.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/*** Global Authentication Status Manager */
object AuthManager {
    // 当前登录状态
    val isLoggedIn = mutableStateOf(false)
    
    // 当前用户信息
    val currentUser = mutableStateOf<User?>(null)
    
    // Firebase认证实例
    private val auth: FirebaseAuth = Firebase.auth
    
    // 初始化函数，检查Firebase Auth状态
    fun initialize() {
        // 添加身份验证状态监听器
        auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            isLoggedIn.value = user != null
            
            if (user != null) {
                // 如果用户已登录，但currentUser为空，则需要从数据库获取完整信息
                if (currentUser.value == null) {
                    // 这里只设置基本信息，完整信息可以通过FirebaseHelper获取
                    currentUser.value = User(
                        uid = user.uid,
                        username = user.displayName ?: "User",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                }
            } else {
                // 用户未登录，清除用户信息
                currentUser.value = null
            }
        }
        
        // 初始检查当前登录状态
        updateAuthState()
    }
    
    // 更新当前认证状态
    fun updateAuthState() {
        val user = auth.currentUser
        isLoggedIn.value = user != null
        
        if (user != null && currentUser.value == null) {
            currentUser.value = User(
                uid = user.uid,
                username = user.displayName ?: "User",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                address = "",  // Default empty value for address
                phone = ""     // Default empty value for phone
            )
        } else if (user == null) {
            currentUser.value = null
        }
    }
    
    // 获取当前FirebaseAuth用户
    fun getCurrentFirebaseUser() = auth.currentUser
} 