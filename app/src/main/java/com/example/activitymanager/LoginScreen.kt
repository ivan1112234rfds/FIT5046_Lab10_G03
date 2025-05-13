package com.example.activitymanager

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "欢迎!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "登录以继续!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            text = AnnotatedString("忘记密码?"),
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
            Text(text = "记住我")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
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
                if (validateInputs(email, password)) {
                    isLoading = true
                    errorMessage = null
                    
                    coroutineScope.launch(Dispatchers.IO) {
                        firebaseHelper.loginUser(
                            email = email,
                            password = password,
                            onSuccess = {
                                isLoading = false
                                // 在主线程显示提示
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "登录成功!", Toast.LENGTH_SHORT).show()
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
                Text("登录", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "- 或者 -",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGoogleSignIn() },
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
            Text("使用 Google 账号登录", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        ClickableText(
            text = AnnotatedString("还不是会员? 立即注册!"),
            onClick = { navController.navigate("register") },
            style = LocalTextStyle.current.copy(
                color = Color(0xFF5A6DF9),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

private fun validateInputs(email: String, password: String): Boolean {
    if (email.isBlank() || password.isBlank()) {
        return false
    }
    
    // 检查邮箱格式
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
