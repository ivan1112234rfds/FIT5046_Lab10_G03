package com.example.activitymanager

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.activitymanager.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.mapper.Activity


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityScreen(navController: NavController, onActivityClick: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }

    val firebaseHelper = FirebaseHelper()
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Search") }
    
    // 添加获取用户偏好的状态
    var userPreference by remember { mutableStateOf("") }
    // 添加活动类型列表状态
    var activityTypes by remember { mutableStateOf<List<String>>(listOf("All", "Design", "Fitness", "Tech", "Hiking")) }

    // 过滤活动
    val filteredActivities = activities.filter {
        (selectedCategory == "All" || it.type == selectedCategory)
    }

    // 获取活动类型
    LaunchedEffect(Unit) {
        try {
            val types = firebaseHelper.getActivityTypes()
            if (types.isNotEmpty()) {
                activityTypes = listOf("All") + types
            }
        } catch (e: Exception) {
            Log.e("ActivityScreen", "Error loading activity types", e)
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 1. 获取当前用户ID
            val currentUser = firebaseHelper.getCurrentUser()
            val uid = currentUser?.uid
            
            // 2. 如果用户已登录，获取用户偏好
            if (uid != null) {
                firebaseHelper.getUserPreferences(
                    uid = uid,
                    onSuccess = { preferences ->
                        userPreference = preferences.activityType
                    },
                    onError = { /* 使用默认空字符串 */ }
                )
            }
            
            // 3. 获取所有活动
            val fetchedActivities = firebaseHelper.getActivities()
            
            // 4. 根据用户偏好和日期对活动进行排序
            activities = fetchedActivities.sortedWith(
                compareBy<Activity> { 
                    // 首先按照类型是否匹配用户偏好排序（不匹配的排后面）
                    if (it.type == userPreference) 0 else 1 
                }.thenBy { 
                    // 然后按日期排序
                    it.date 
                }
            )
            
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
            Log.e("ActivityScreen", "Error loading activities", e)
        }
    }
    
    Scaffold(
        modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {selectedTab = it},
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Featured Activities",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChipsRow(
                    options = activityTypes,
                    selected = selectedCategory,
                    onSelectedChange = { selectedCategory = it }
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (error != null) {
                    Text(
                        "Error: $error", 
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredActivities) { activity ->
                            ActivityItem(
                                activity = activity,
                                onClick = { onActivityClick(activity.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelectedChange(option) },
                label = { Text(option) },
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF5A6DF9),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0F0F0),
                    labelColor = Color.Black
                )
            )
        }
    }
}



