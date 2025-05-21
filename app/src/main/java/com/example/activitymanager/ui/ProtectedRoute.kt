package com.example.activitymanager.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 受保护的路由组件
 * 用于检查用户是否已登录，如果未登录则显示提示对话框并执行相应操作
 */
@Composable
fun ProtectedRoute(
    isLoggedIn: Boolean,
    onNotLoggedIn: () -> Unit,
    content: @Composable () -> Unit
) {
    var showLoginDialog by remember { mutableStateOf(false) }
    
    if (isLoggedIn) {
        // 用户已登录，显示受保护的内容
        content()
    } else {
        // 用户未登录，触发回调并显示对话框
        LaunchedEffect(Unit) {
            showLoginDialog = true
            onNotLoggedIn()
        }
    }
    
    // 登录提醒对话框
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login Required") },
            text = { Text("You need to be logged in to access this feature.") },
            confirmButton = {
                Button(onClick = { 
                    showLoginDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }
} 