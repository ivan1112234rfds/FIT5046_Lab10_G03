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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.activitymanager.mapper.Activity
import com.example.activitymanager.R
import com.example.activitymanager.createMockActivities


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    // 模拟活动列表数据，实际项目中可从网络/数据库获取
    val activities = createMockActivities()

    Column(modifier = modifier.fillMaxSize()) {
        // 1. 顶部 Header，使用 Material 3 的 CenterAlignedTopAppBar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Hi, Fan",
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

        // 2. Banner 区域
        BannerSection()

        Spacer(modifier = Modifier.height(16.dp))

        // 3. “Top Rated” 标题区域
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
                    // 点击 “View All” 后的操作
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. 活动列表 —— 使用 LazyColumn 显示活动卡片
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

@Composable
fun BannerSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(
                // 使用 Material 3 的颜色体系
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
            // 左侧：文字和按钮
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
                    onClick = { /* 处理按钮点击 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Get Now")
                }
            }
            // 右侧：图片展示
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
            // 活动图片，替换成你实际的图片资源
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_activity_image),
                contentDescription = activity.title,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // 活动文本信息
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