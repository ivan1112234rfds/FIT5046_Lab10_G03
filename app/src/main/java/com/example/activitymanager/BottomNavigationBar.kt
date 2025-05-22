package com.example.activitymanager

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush

import com.example.activitymanager.BottomNavItem
import androidx.navigation.NavController


@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    navController: NavController
) {
    BottomAppBar(
        tonalElevation = 8.dp,
        containerColor = Color.White
    ) {
        BottomNavItem(
            label = "Home",
            icon = Icons.Default.Home,
            selected = selectedTab == "Home",
            onClick = { 
                onTabSelected("Home")
                navController.navigate("HomeScreen") 
            },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            label = "My Activity",
            icon = Icons.Default.MenuBook,
            selected = selectedTab == "My Activity",
            onClick = { 
                onTabSelected("My Activity")
                navController.navigate("activities") 
            },
            modifier = Modifier.weight(1f)
        )

        // 中间圆形按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // 添加光晕效果
            if (selectedTab == "Search") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            
            FloatingActionButton(
                onClick = {
                    onTabSelected("Search")
                    if (navController.currentDestination?.route != "activityList") {
                        navController.navigate("activityList")
                    }
                },
                containerColor = if (selectedTab == "Search") Color(0xFF6366F1) else Color(0xFF6366F1),
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
            onClick = { 
                onTabSelected("Manage")
                navController.navigate("Manage") 
            },
            modifier = Modifier.weight(1f)
        )

        BottomNavItem(
            label = "Profile",
            icon = Icons.Default.Person,
            selected = selectedTab == "Profile",
            onClick = { 
                onTabSelected("Profile")
                navController.navigate("Profile") 
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color(0xFF6366F1) else Color.Gray
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) Color(0xFF6366F1) else Color.Gray
        )
    }
}