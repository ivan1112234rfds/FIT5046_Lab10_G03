package com.example.activitymanager

import CreateActivityScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.activitymanager.ui.theme.ActivityManagerTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }
        setContent {
            ActivityTheme {
                ActivityApp()
            }
        }
    }
}
@Composable
fun ActivityApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "activities") {
        composable("activities") {
            MyActivitiesScreen(
                onActivityClick = { activityId ->
                    navController.navigate("activity_details/$activityId")
                },
                onCreateActivityClick = {
                    navController.navigate("create_activity")
                }
            )
        }
        composable(
            route = "activity_details/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            ActivityDetailsScreen(
                activityId = activityId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("create_activity") {
            CreateActivityScreen(
                onNavigateBack = { navController.popBackStack() },
                onActivityCreated = { newActivity ->
                }
            )
        }
    }
}
@Composable
fun ActivityTheme(content: @Composable () -> Unit) {
    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF4A6EFF),
        onPrimary = Color.White,
        secondary = Color(0xFFFF4A6E),
        background = Color(0xFFF8F9FC),
        surface = Color.White,
        onSurface = Color(0xFF2A2A2A)
    )

    MaterialTheme(
        colorScheme = lightColorScheme,
        content = content
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyActivitiesScreen(onActivityClick: (String) -> Unit, onCreateActivityClick: () -> Unit) {
    val activities = remember { createMockActivities() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val filteredActivities = when (selectedTabIndex) {
        0 -> activities.filter { it.date.after(Date()) } // Upcoming
        1 -> activities.filter { it.date.before(Date()) } // Past
        else -> activities // All
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activities") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }, floatingActionButton = {
            FloatingActionButton(onClick = onCreateActivityClick) {
                Icon(Icons.Default.Add, contentDescription = "创建活动")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 选项卡
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                listOf("Upcoming", "Past", "All").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // 活动列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 活动图片 (模拟)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E5FF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 活动信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "by ${activity.organizer}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 评分和时长
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )

                    Text(
                        text = activity.rating.toString(),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 2.dp, end = 8.dp)
                    )

                    Text(
                        text = "• ${activity.duration}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${activity.participants} participants",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // 右侧价格和收藏图标
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { /* 收藏功能 */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (activity.isFavorite) Icons.Outlined.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (activity.isFavorite) MaterialTheme.colorScheme.secondary else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailsScreen(activityId: String, onBackClick: () -> Unit) {
    // 在实际应用中，这应该从数据库或API获取
    val allActivities = remember { createMockActivities() }
    val activity = remember { allActivities.find { it.id == activityId } }

    if (activity == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Activity not found")
        }
        return
    }

    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activity.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 活动图片
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E5FF))
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 基本信息
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = activity.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Organized by ${activity.organizer}",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${activity.rating} • ${activity.participants} participants",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 日期和时长
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = dateFormat.format(activity.date),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = activity.duration,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 价格和注册按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { /* 注册活动 */ },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Register Now")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 活动描述
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About this activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = activity.description,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 位置信息和地图
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = activity.location,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google Maps 集成
                        // 注意：这需要一个有效的Google Maps API密钥和相应的权限
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0E5FF))
                        ) {
                            // 实际应用中，这里应该是GoogleMap组件
                            // GoogleMap(
                            //    modifier = Modifier.fillMaxSize(),
                            //    cameraPositionState = rememberCameraPositionState {
                            //        position = CameraPosition.fromLatLngZoom(activity.coordinates, 15f)
                            //    }
                            // ) {
                            //    Marker(
                            //        state = MarkerState(position = activity.coordinates),
                            //        title = activity.title
                            //    )
                            // }

                            // 由于这是示例，我们使用一个占位符
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Google Map will be displayed here")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { /* 打开地图应用 */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Directions")
                        }
                    }
                }
            }
        }
    }
}

// 数据模型
data class Activity(
    val id: String,
    val title: String,
    val description: String,
    val date: Date,
    val location: String,
    val organizer: String,
    val rating: Double,
    val duration: String,
    val participants: Int,
    val isFavorite: Boolean = false,
    val coordinates: LatLng = LatLng(0.0, 0.0)  // 添加位置坐标
)


// 创建模拟数据
fun createMockActivities(): List<Activity> {
    val cal = Calendar.getInstance()
    val mockActivities = mutableListOf<Activity>()

    // 即将到来的活动
    cal.add(Calendar.DAY_OF_YEAR, 1)
    mockActivities.add(
        Activity(
            id = "1",
            title = "Art Masterclass",
            description = "Join our comprehensive Art Masterclass where you'll learn advanced techniques from professional artists. This workshop covers various mediums including acrylics, oils, and watercolors. Perfect for intermediate to advanced artists looking to enhance their skills.",
            date = cal.time,
            location = "Art Studio 42, 123 Creative Avenue, San Francisco",
            organizer = "Roy Marsh",
            rating = 4.9,
            duration = "2h 30mins",
            participants = 1245,
            isFavorite = true,
            coordinates = LatLng(37.7749, -122.4194)  // San Francisco coordinates
        )
    )

    cal.add(Calendar.DAY_OF_YEAR, 2)
    mockActivities.add(
        Activity(
            id = "2",
            title = "Figma Mastery",
            description = "Become a Figma expert with this comprehensive course. Learn everything from the basics to advanced prototyping, design systems, and collaboration features. By the end of this course, you'll be able to create professional-grade UI/UX designs efficiently.",
            date = cal.time,
            location = "Online",
            organizer = "Annie Chandler",
            rating = 4.2,
            duration = "3h 30mins",
            participants = 2182,
            coordinates = LatLng(0.0, 0.0)  // Online course
        )
    )
    return mockActivities
}

