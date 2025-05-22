package com.example.activitymanager.firebase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.activitymanager.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * 全局身份验证状态管理器
 */
object AuthManager {
    val isLoggedIn = mutableStateOf(false)
    
    val currentUser = mutableStateOf<User?>(null)
    
    private val auth: FirebaseAuth = Firebase.auth
    
    fun initialize() {
        auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            isLoggedIn.value = user != null
            
            if (user != null) {
                if (currentUser.value == null) {
                    currentUser.value = User(
                        uid = user.uid,
                        username = user.displayName ?: "User",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                }
            } else {
                currentUser.value = null
            }
        }
        
        updateAuthState()
    }
    
    fun updateAuthState() {
        val user = auth.currentUser
        isLoggedIn.value = user != null
        
        if (user != null && currentUser.value == null) {
            currentUser.value = User(
                uid = user.uid,
                username = user.displayName ?: "User",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: ""
            )
        } else if (user == null) {
            currentUser.value = null
        }
    }
    
    fun getCurrentFirebaseUser() = auth.currentUser
} 