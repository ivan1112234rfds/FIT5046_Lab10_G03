package com.example.fit5046assignment

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
import com.example.assignmentcode.BottomNavigationBar
import com.example.activitymanager.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    // 创建状态
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Home") }
    
    // 添加用户名状态
    var username by remember { mutableStateOf("") }
    
    // 获取上下文
    val context = LocalContext.current
    
    // 获取FirebaseHelper实例
    val firebaseHelper = remember { FirebaseHelper() }
    
    // 获取用户名
    LaunchedEffect(Unit) {
        try {
            val userDao = AppDatabase.getInstance(context).userDao()
            val currentUser = withContext(Dispatchers.IO) {
                userDao.getCurrentUser()
            }
            
            // 如果本地有用户数据，使用本地数据
            if (currentUser != null && currentUser.username.isNotEmpty()) {
                username = currentUser.username
            } 
            // 如果本地没有用户名，从Firebase获取
            else if (firebaseHelper.getCurrentUser() != null) {
                val uid = firebaseHelper.getCurrentUser()?.uid ?: ""
                firebaseHelper.getUserData(
                    uid = uid,
                    onSuccess = { user ->
                        username = user.username
                    },
                    onError = { errorMsg ->
                        // 如果获取失败，使用默认名称
                        username = "User"
                    }
                )
            }
        } catch (e: Exception) {
            // 如果出错，使用默认名称
            username = "User"
        }
    }
    
    // 使用LaunchedEffect在屏幕加载时获取数据
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 获取所有活动
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
                        // 使用动态用户名
                        text = "Hi, ${if (username.isNotEmpty()) username else "Fan"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* 处理通知点击事件 */ }) {
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

            // 根据加载状态显示不同内容
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
                        Text("加载失败: $error", color = Color.Red)
                    }
                }
                activities.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无活动数据")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(activities) { activity ->
                            ActivityCard(activity)
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
                painter = painterResource(id = R.drawable.ic_banner_image), // 替换成你项目中的 Banner 图片
                contentDescription = "Banner Illustration",
                modifier = Modifier
                    .size(120.dp)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun ActivityCard(activity: Activity) {
    // 根据活动类型选择图片
    val imageRes = when (activity.type) {
        "Movie" -> R.drawable.movie
        "Hiking" -> R.drawable.hiking
        else -> R.drawable.pic1
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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