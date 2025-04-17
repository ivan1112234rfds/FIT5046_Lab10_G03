package com.example.activitymanager



import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.activitymanager.mapper.Activity
import com.example.assignmentcode.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyActivitiesScreen(onActivityClick: (String) -> Unit, onCreateActivityClick: () -> Unit) {
    val activities = remember { createMockActivities() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Past", "All")

    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf("Home") }

    val filteredActivities = when (selectedTabIndex) {
        0 -> activities.filter { it.date.after(Date()) } // Upcoming
        1 -> activities.filter { it.date.before(Date()) } // Past
        else -> activities // All
    }


    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {selectedTab = it}
            )
        }, floatingActionButton = {
            FloatingActionButton(onClick = onCreateActivityClick) {
                Icon(Icons.Default.Add, contentDescription = "Create Activities")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "My Activity",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // 选项卡
            TabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color.Blue else Color.Gray
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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


@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())


    val imageRes = when (activity.type) {
        "Movie" -> R.drawable.movie
        "Hiking" -> R.drawable.hiking
        else -> R.drawable.pic1
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 活动图片 (模拟)
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Activity Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 活动信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = activity.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "by ${activity.organizer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "" + activity.rating + " " + activity.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${activity.participants} participants",
                    style = MaterialTheme.typography.bodySmall,
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
