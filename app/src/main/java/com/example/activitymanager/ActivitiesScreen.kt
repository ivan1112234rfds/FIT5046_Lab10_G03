package com.example.activitymanager

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.assignmentcode.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityScreen(navController: NavController, onActivityClick: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }

    val activities = remember { createMockActivities() }
    val filteredActivities = activities.filter {
        (selectedCategory == "All" || it.type == selectedCategory)
    }
    var selectedTab by remember { mutableStateOf("Home") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {selectedTab = it},
                navController = navController
            )
        }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp)
        ) {
            Text(
                "Featured Activities",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(Modifier.height(12.dp))
            FilterChipsRow(
                options = listOf("All", "Design", "Fitness", "Tech", "Hiking"),
                selected = selectedCategory,
                onSelectedChange = { selectedCategory = it }
            )

            Spacer(Modifier.height(8.dp))

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

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A6DF9))
            ) {
                Text("Go to Login", color = Color.White)
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
    Row(modifier = Modifier.fillMaxWidth()) {
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

