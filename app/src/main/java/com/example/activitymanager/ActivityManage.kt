package com.example.activitymanager

import CreateActivityScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.activitymanager.BottomNavigationBar
import androidx.compose.ui.platform.LocalContext
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.mapper.Activity
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.activitymanager.ui.theme.ActivityManagerTheme
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

@Composable
fun ActivityManageScreen(navController : NavController) {
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Active", "Closed")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf("Manage") }
    var activityList by remember { mutableStateOf<List<Activity>>(emptyList()) }

    // State for create activity dialog
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    // Function to refresh activity list
    val refreshActivities = {
        coroutineScope.launch {
            val currentUser = firebaseHelper.getCurrentUser()
            val currentUserId = currentUser?.uid ?: ""
            activityList = firebaseHelper.getActivities(currentUserId)
        }
    }

    LaunchedEffect(Unit) {
        refreshActivities()
    }

    // Create activity dialog
    if (showCreateDialog) {
        Dialog(
            onDismissRequest = { showCreateDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box {
                    // We reuse the existing CreateActivityScreen but adapt it for dialog use
                    CreateActivityScreen(
                        onNavigateBack = { showCreateDialog = false },
                        onActivityCreated = {
                            showCreateDialog = false
                            refreshActivities()
                        }
                    )
                    // Add a close button at the top-right corner
                    IconButton(
                        onClick = { showCreateDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && activityToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                activityToDelete = null
            },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${activityToDelete?.title}?") },
            confirmButton = {
                TextButton(onClick = {
                    // Implement deletion functionality
                    coroutineScope.launch {
                        activityToDelete?.id?.let { id ->
                            firebaseHelper.deleteActivity(
                                activityId = id,
                                onSuccess = {
                                    refreshActivities()
                                },
                                onError = {
                                    // Show error message
                                }
                            )
                        }
                    }
                    showDeleteDialog = false
                    activityToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    activityToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
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
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {

            Text(
                text = "Activity Management",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
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
            val formatter = SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss z", Locale.ENGLISH)

            // Filter activities based on selected tab
            val filteredActivities = if (selectedTabIndex == 0) {
                // Active tab - show current and future activities
                activityList.filter { activity ->
                    activity.date?.after(java.util.Date()) ?: true
                }
            } else {
                // Closed tab - show past activities
                activityList.filter { activity ->
                    activity.date?.before(java.util.Date()) ?: false
                }
            }

            if (filteredActivities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${if (selectedTabIndex == 0) "active" else "closed"} activities found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredActivities) { activity ->
                        val formattedDate = activity.date?.let { formatter.format(it) } ?: ""
                        ActivityCard(
                            title = activity.title ?: "",
                            author = activity.organizer ?: "",
                            rating = activity.rating ?: 0.0,
                            duration = formatDuration(activity.duration ?: ""),
                            participants = activity.participants ?: 0,
                            liked = false,
                            type = activity.type ?: "",
                            showActions = (selectedTabIndex == 0),
                            onEditClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.apply {
                                    set("id", activity.id ?: "")
                                    set("title", activity.title ?: "")
                                    set("description", activity.description ?: "")
                                    set("location", activity.location ?: "")
                                    set("date", formattedDate)
                                    set("duration", activity.duration ?: "")
                                    set("organizer", activity.organizer ?: "")
                                    set("type", activity.type ?: "")
                                    set("participants", activity.participants?.toString() ?: "0")
                                    set("participantsIDs", activity.participantsIDs ?: emptyList())
                                    set("coordinates", mapOf(
                                        "latitude" to activity.coordinates?.latitude,
                                        "longitude" to activity.coordinates?.longitude
                                    ))
                                }
                                navController.navigate("edit_activity")
                            },
                            onDeleteClick = {
                                activityToDelete = activity
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // "Add Activity" button
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Activity")
            }
        }
    }
}

// ActivityCard component remains the same
@Composable
fun ActivityCard(
    title: String,
    author: String,
    rating: Double,
    duration: String,
    participants: Int,
    liked: Boolean,
    type: String,
    showActions: Boolean = true,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val imageRes = when (type.lowercase()) {
        "movie" -> R.drawable.movie
        "hiking" -> R.drawable.hiking
        else -> R.drawable.pic1
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "by $author",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
                        text = "$rating  •  $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "$participants participants limit",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (showActions) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatDuration(durationStr: String): String {
    val totalMinutes = durationStr.toIntOrNull() ?: return ""
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}mins"
        hours > 0 && minutes == 0 -> "${hours}h"
        else -> "${minutes}mins"
    }
}