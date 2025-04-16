package com.example.assignmentcode

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import com.example.activitymanager.BottomNavItem

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    BottomAppBar(
        tonalElevation = 8.dp,
        containerColor = Color.White
    ) {
        BottomNavItem(
            label = "Home",
            icon = Icons.Default.Home,
            selected = selectedTab == "Home",
            onClick = { onTabSelected("Home") },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            label = "My Activity",
            icon = Icons.Default.MenuBook,
            selected = selectedTab == "My Activity",
            onClick = { onTabSelected("My Activity") },
            modifier = Modifier.weight(1f)
        )

        // 中间圆形按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = { /* 搜索动作 */ },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        BottomNavItem(
            label = "Manage",
            icon = Icons.Default.ChatBubble,
            selected = selectedTab == "Manage",
            onClick = { onTabSelected("Manage") },
            modifier = Modifier.weight(1f)
        )

        BottomNavItem(
            label = "Profile",
            icon = Icons.Default.Person,
            selected = selectedTab == "Profile",
            onClick = { onTabSelected("Profile") },
            modifier = Modifier.weight(1f)
        )
    }
}