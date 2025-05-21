package com.example.activitymanager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.activitymanager.mapper.Activity
import com.example.activitymanager.R
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.firebase.AuthManager
import com.example.activitymanager.BottomNavigationBar
import com.example.activitymanager.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Home") }
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    
    // 从AuthManager获取登录状态
    val isLoggedIn by AuthManager.isLoggedIn
    val currentUser by AuthManager.currentUser

    LaunchedEffect(isLoggedIn) {
        try {
            if (isLoggedIn) {
                // 获取当前用户ID
                val user = currentUser // 将委托属性赋值给本地变量
                val uid = user?.uid ?: ""
                
                if (uid.isNotEmpty()) {
                    // 直接从Firebase数据库获取用户信息
                    firebaseHelper.getUserData(
                        uid = uid,
                        onSuccess = { user ->
                            username = user.username // 使用数据库中的username
                        },
                        onError = { errorMsg ->
                            username = user?.username ?: "User" // 降级使用AuthManager中的值
                        }
                    )
                } else {
                    username = ""
                }
            } else {
                // 用户未登录
                username = ""
            }
        } catch (e: Exception) {
            username = ""
        }
    }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val fetchedActivities = firebaseHelper.getActivities()
            activities = fetchedActivities
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                navController = navController
            )
        }
    ) { innerPadding ->
        Column(modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isLoggedIn && username.isNotEmpty()) "Hi, $username" else "Not Logged In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle the notification click event */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification),
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )


            BannerSection(navController)

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Rated",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        // 点击 "View All" 后的操作
                        navController.navigate("activityList")
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading failure: $error", color = Color.Red)
                    }
                }
                activities.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("There is no activity data for the time being")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(activities) { activity ->
                            ActivityCard(
                                activity = activity,
                                onActivityClick = { activityId ->
                                    navController.navigate("activity_details/$activityId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BannerSection(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(

                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enjoy and Get Activities",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("activityList") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Get Now")
                }
            }

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_banner_image),
                contentDescription = "Banner Illustration",
                modifier = Modifier
                    .size(120.dp)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun ActivityCard(activity: Activity, onActivityClick: (String) -> Unit = {}) {
    val imageRes = when (activity.type) {
        "Movie" -> R.drawable.movie
        "Hiking" -> R.drawable.hiking
        else -> R.drawable.pic1
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onActivityClick(activity.id) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            androidx.compose.foundation.Image(
                painter = painterResource(id = imageRes),
                contentDescription = activity.title,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Date: ${activity.date}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Rating: ${activity.rating} | Participants: ${activity.participants}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}