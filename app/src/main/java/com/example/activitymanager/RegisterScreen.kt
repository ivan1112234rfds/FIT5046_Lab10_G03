package com.example.activitymanager

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun RegisterScreen(navController: NavController) {
    // Required fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Optional fields
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Validation errors
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Function to show date picker dialog
    fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day -> 
                birthday = "$year-${month + 1}-$day" 
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Sign up to get started!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Username field with validation
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                usernameError = null 
            },
            label = { Text("Username *") },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError != null,
            supportingText = {
                if (usernameError != null) {
                    Text(
                        text = usernameError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        // Email field with validation
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                emailError = null 
            },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field with validation
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = null 
            },
            label = { Text("Password *") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF5A6DF9))
                }
            },
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Gender:", modifier = Modifier.align(Alignment.Start))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5A6DF9))
            )
            Text("Male")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5A6DF9))
            )
            Text("Female")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional age field - restricted to numbers only
        OutlinedTextField(
            value = age,
            onValueChange = { 
                // Only accept digits
                if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                    age = it
                }
            },
            label = { Text("Age (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Optional birthday field - changed to date picker
        OutlinedTextField(
            value = birthday,
            onValueChange = { /* Read-only */ },
            label = { Text("Birthday (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker() },
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // Optional address field
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address (Optional)") },
            modifier = Modifier.fillMaxWidth(),
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // Optional phone field - restricted to numbers, plus and dash
        OutlinedTextField(
            value = phone,
            onValueChange = { 
                // Only accept digits, plus sign and hyphen
                if (it.all { char -> char.isDigit() || char == '+' || char == '-' } || it.isEmpty()) {
                    phone = it
                }
            },
            label = { Text("Phone (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                // Validate the required fields
                val isValid = validateInputs(
                    username = username,
                    email = email,
                    password = password,
                    usernameError = { usernameError = it },
                    emailError = { emailError = it },
                    passwordError = { passwordError = it }
                )
                
                if (isValid) {
                    // Create user object
                    val user = User(
                        username = username,
                        nickname = username, // Use username as nickname for compatibility
                        gender = gender,
                        age = age,
                        birthday = birthday,
                        email = email,
                        address = address,
                        phone = phone
                    )
                    
                    // Register user
                    isLoading = true
                    errorMessage = null
                    
                    coroutineScope.launch(Dispatchers.IO) {
                        firebaseHelper.registerUser(
                            email = email,
                            password = password,
                            user = user,
                            onSuccess = {
                                isLoading = false
                                // Show toast on main thread
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login")
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
                Text("Register", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ClickableText(
            text = AnnotatedString("Already have an account? Login"),
            onClick = { navController.navigate("login") },
            style = LocalTextStyle.current.copy(color = Color(0xFF5A6DF9), fontSize = 14.sp)
        )
    }
}

// Improved validation function with specific error messages
private fun validateInputs(
    username: String,
    email: String,
    password: String,
    usernameError: (String) -> Unit,
    emailError: (String) -> Unit,
    passwordError: (String) -> Unit
): Boolean {
    var isValid = true
    
    // Username validation
    if (username.isBlank()) {
        usernameError("Username is required")
        isValid = false
    }
    
    // Email validation
    if (email.isBlank()) {
        emailError("Email is required")
        isValid = false
    } else {
        // Check email format
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!email.matches(emailPattern.toRegex())) {
            emailError("Invalid email format")
            isValid = false
        }
    }
    
    // Password validation
    if (password.isBlank()) {
        passwordError("Password is required")
        isValid = false
    } else if (password.length < 6) {
        passwordError("Password must be at least 6 characters")
        isValid = false
    }
    
    return isValid
}

//@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}

