package com.example.activitymanager

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.activitymanager.R
import com.example.activitymanager.firebase.FirebaseHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.activitymanager.AppDatabase
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onGoogleSignIn: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    TextButton(onClick = {
                        navController.navigate("HomeScreen") {
                            popUpTo("login") { inclusive = true }
                        }
                    }) {
                        Text("Skip Login", color = Color(0xFF5A6DF9))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // background pic
            Image(
                painter = painterResource(id = R.drawable.login_background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                alpha = 0.30f, // Set high transparency（0.0 ~ 1.0）
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Sign in to continue!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF5A6DF9))
                        }
                    }
                )
                ClickableText(
                    text = AnnotatedString("Forgot password?"),
                    onClick = { navController.navigate("forgot_password") },
                    style = LocalTextStyle.current.copy(
                        color = Color(0xFF5A6DF9),
                        fontSize = 14.sp,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF5A6DF9))
                    )
                    Text(text = "Remember me")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (validateInputs(email, password)) {
                            isLoading = true
                            errorMessage = null
                            val userDao = AppDatabase.getInstance(context).userDao()

                            coroutineScope.launch(Dispatchers.IO) {
                                firebaseHelper.loginUser(
                                    email = email,
                                    password = password,
                                    userDao = userDao,
                                    onSuccess = {
                                        isLoading = false
                                        // Show toast on main thread
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("HomeScreen") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Sign in", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "- OR -",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val auth = Firebase.auth

                                firebaseHelper.checkAuthState()

                                val usingEmulator = true
                                if (usingEmulator) {
                                    try {
                                        auth.useEmulator("10.0.2.2", 9099)
                                        Log.d("LoginScreen", "Connected to Auth Emulator for Google login")
                                    } catch (e: Exception) {
                                        Log.e("LoginScreen", "Failed to connect to Auth Emulator", e)
                                    }
                                }

                                val testEmail = "test@gmail.com"
                                val testPassword = "admin123456!"

                                try {
                                    auth.signInWithEmailAndPassword(testEmail, testPassword)
                                        .addOnSuccessListener {
                                            val user = auth.currentUser
                                            Log.d("LoginScreen", "Emulator Google login successful! User: ${user?.uid}")

                                            firebaseHelper.checkAuthState()

                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast.makeText(context, "Google login successful!", Toast.LENGTH_SHORT).show()
                                                navController.navigate("HomeScreen") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast.makeText(context, "Google login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                Toast.makeText(context, "Please create test user in Auth Emulator", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } catch (e: Exception) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google (Test)", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(24.dp))

                ClickableText(
                    text = AnnotatedString("Not a member? Register now!"),
                    onClick = { navController.navigate("register") },
                    style = LocalTextStyle.current.copy(
                        color = Color(0xFF5A6DF9),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

private fun validateInputs(email: String, password: String): Boolean {
    if (email.isBlank() || password.isBlank()) {
        return false
    }
    
    // Check email format
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    if (!email.matches(emailPattern.toRegex())) {
        return false
    }
    
    return true
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
