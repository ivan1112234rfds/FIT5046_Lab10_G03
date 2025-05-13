package com.example.activitymanager

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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

@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "创建账号",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "注册以开始使用!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF5A6DF9))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("性别:", modifier = Modifier.align(Alignment.Start))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5A6DF9))
            )
            Text("男")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5A6DF9))
            )
            Text("女")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("年龄") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = birthday,
            onValueChange = { birthday = it },
            label = { Text("生日 (例如: 2000-01-01)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 显示错误信息
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
                if (validateInputs(username, email, password, nickname, age, birthday)) {
                    // 创建用户对象
                    val user = User(
                        username = username,
                        nickname = nickname,
                        gender = gender,
                        age = age,
                        birthday = birthday,
                        email = email
                    )
                    
                    // 注册用户
                    isLoading = true
                    errorMessage = null
                    
                    coroutineScope.launch(Dispatchers.IO) {
                        firebaseHelper.registerUser(
                            email = email,
                            password = password,
                            user = user,
                            onSuccess = {
                                isLoading = false
                                // 在主线程显示提示
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "注册成功!", Toast.LENGTH_SHORT).show()
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
                Text("注册", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ClickableText(
            text = AnnotatedString("已有账号? 登录"),
            onClick = { navController.navigate("login") },
            style = LocalTextStyle.current.copy(color = Color(0xFF5A6DF9), fontSize = 14.sp)
        )
    }
}

private fun validateInputs(
    username: String,
    email: String,
    password: String,
    nickname: String,
    age: String,
    birthday: String
): Boolean {
    if (username.isBlank() || email.isBlank() || password.isBlank()) {
        return false
    }
    
    // 检查邮箱格式
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    if (!email.matches(emailPattern.toRegex())) {
        return false
    }
    
    // 检查密码长度
    if (password.length < 6) {
        return false
    }
    
    return true
}

//@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}

