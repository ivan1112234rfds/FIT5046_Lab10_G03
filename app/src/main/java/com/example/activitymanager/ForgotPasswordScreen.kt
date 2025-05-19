package com.example.activitymanager

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.activitymanager.firebase.FirebaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enter your registered email and we'll send you a password reset link",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                // Clear previous messages
                errorMessage = null
                successMessage = null
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Display error message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Display success message
        successMessage?.let {
            Text(
                text = it,
                color = Color(0xFF4CAF50), // Green
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (validateEmail(email)) {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    
                    coroutineScope.launch(Dispatchers.IO) {
                        firebaseHelper.sendPasswordResetEmail(
                            email = email,
                            onSuccess = {
                                isLoading = false
                                successMessage = "Password reset link has been sent! Please check your email and click the link to reset your password."
                                
                                // Show toast on main thread
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "Reset link sent!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = "Sending failed: $error"
                            }
                        )
                    }
                } else {
                    errorMessage = "Please enter a valid email address"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && email.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Send Reset Link", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ClickableText(
            text = AnnotatedString("Back to Login"),
            onClick = { navController.popBackStack() },
            style = LocalTextStyle.current.copy(color = Color(0xFF5A6DF9), fontSize = 14.sp)
        )
    }
}

private fun validateEmail(email: String): Boolean {
    if (email.isBlank()) return false
    
    // Simple email format validation
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}
