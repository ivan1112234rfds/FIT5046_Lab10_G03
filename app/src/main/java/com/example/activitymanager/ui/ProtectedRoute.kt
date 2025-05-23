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
 * Protected routing component
 * It is used to check whether the user is logged in. If not, a prompt dialog box will be displayed and the corresponding operation will be performed
 */
@Composable
fun ProtectedRoute(
    isLoggedIn: Boolean,
    onNotLoggedIn: () -> Unit,
    content: @Composable () -> Unit
) {
    var showLoginDialog by remember { mutableStateOf(false) }
    
    if (isLoggedIn) {
        // The user has logged in and protected content is displayed
        content()
    } else {
        // When the user is not logged in, a callback is triggered and a dialog box is displayed
        LaunchedEffect(Unit) {
            showLoginDialog = true
            onNotLoggedIn()
        }
    }
    
    // Login reminder dialog box
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