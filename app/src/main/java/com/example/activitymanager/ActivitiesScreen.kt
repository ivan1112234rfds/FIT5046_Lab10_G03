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
import android.app.DatePickerDialog
import java.util.Calendar
import java.util.Date
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.*


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityScreen(navController: NavController, onActivityClick: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }

    val firebaseHelper = FirebaseHelper()
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Search") }
    
    // Add the status for obtaining user preferences
    var userPreference by remember { mutableStateOf("") }

    // Add the status of obtaining user preferences and the status of the list of activity types
    var activityTypes by remember { mutableStateOf<List<String>>(listOf("All", "Design", "Fitness", "Tech", "Hiking")) }

    // Filtering activity
    val filteredActivities = activities.filter {
        (selectedCategory == "All" || it.type == selectedCategory)
    }

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    // get all activity types
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
            // 1. get current user id
            val currentUser = firebaseHelper.getCurrentUser()
            val uid = currentUser?.uid
            
            // 2. get user preferences, if login
            if (uid != null) {
                firebaseHelper.getUserPreferences(
                    uid = uid,
                    onSuccess = { preferences ->
                        userPreference = preferences.activityType
                    },
                    onError = {  }
                )
            }
            
            // 3. get all activities
            val fetchedActivities = firebaseHelper.getActivities(
                startDate = startDate,
                endDate = endDate
            )
            
            // 4. sort with date and preferences
            activities = fetchedActivities.sortedWith(
                compareBy<Activity> {
                    if (it.type == userPreference) 0 else 1 
                }.thenBy {
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

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            var startDate by remember { mutableStateOf<Date?>(null) }
            var endDate by remember { mutableStateOf<Date?>(null) }
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val buttonModifier = Modifier
                .weight(1f)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF5A6DF9))
                .padding(horizontal = 12.dp, vertical = 4.dp)

            val buttonTextStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = buttonModifier.clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth, 0, 0, 0)
                            startDate = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(
                        text = startDate?.let { dateFormat.format(it) } ?: "Start Date",
                        style = buttonTextStyle,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Box(modifier = buttonModifier.clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth, 23, 59, 59)
                            endDate = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(
                        text = endDate?.let { dateFormat.format(it) } ?: "End Date",
                        style = buttonTextStyle,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Box(
                    modifier = buttonModifier.clickable {
                        coroutineScope.launch {
                            val currentUser = firebaseHelper.getCurrentUser()
                            val uid = currentUser?.uid
                            activities = firebaseHelper.getActivities(
                                uid = uid,
                                startDate = startDate,
                                endDate = endDate
                            )
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Search", style = buttonTextStyle)
                }
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



