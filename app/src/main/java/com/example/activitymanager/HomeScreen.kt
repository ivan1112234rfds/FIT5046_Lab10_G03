package com.example.activitymanager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.activitymanager.mapper.Activity
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.firebase.AuthManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.DpOffset

// Add this new carousel component
@Composable
fun BannerCarousel(
    imageResources: List<Int>,         // List of image resource IDs
    autoScrollDuration: Long = 5000,   // Auto-scroll interval in milliseconds
    content: @Composable (BoxScope.() -> Unit)? = null // Optional content to display over the carousel
) {
    // Current page index
    var currentPage by remember { mutableStateOf(0) }
    
    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while(true) {
            delay(autoScrollDuration)
            currentPage = (currentPage + 1) % imageResources.size
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Display current image
        androidx.compose.foundation.Image(
            painter = painterResource(id = imageResources[currentPage]),
            contentDescription = "Banner image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            imageResources.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == currentPage) 
                                Color.White 
                            else 
                                Color.Gray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            }
        }
        
        // Optional content over the carousel
        content?.invoke(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Home") }
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }

    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Get login status from AuthManager
    val isLoggedIn by AuthManager.isLoggedIn
    val currentUser by AuthManager.currentUser

    LaunchedEffect(isLoggedIn) {
        try {
            if (isLoggedIn) {
                // Get current user ID
                val user = currentUser // Assign delegated property to local variable
                val uid = user?.uid ?: ""
                
                if (uid.isNotEmpty()) {
                    // Get user information directly from Firebase database
                    firebaseHelper.getUserData(
                        uid = uid,
                        onSuccess = { user ->
                            username = user.username // Use username from database
                        },
                        onError = { errorMsg ->
                            username = user?.username ?: "User" // Fallback to use value from AuthManager
                        }
                    )
                } else {
                    username = ""
                }
            } else {
                // User not logged in
                username = ""
            }
        } catch (e: Exception) {
            username = ""
        }
    }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val fetchedActivities = firebaseHelper.getActivities()
            // Sort activities by date, most recent first
            activities = fetchedActivities.sortedBy { it.date }
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User avatar or initials
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoggedIn && username.isNotEmpty()) 
                                  username.first().toString().uppercase() 
                                  else "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = if (isLoggedIn && username.isNotEmpty()) username else "Guest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // hamburg menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = (-32).dp, y = 8.dp)
                        ) {
                            if (isLoggedIn) {
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        showMenu = false
                                        showLogoutDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Login") },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("login")
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Dashboard") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("dashboard")
                                }
                            )
                        }

                        if (showLogoutDialog) {
                            AlertDialog(
                                onDismissRequest = { showLogoutDialog = false },
                                title = { Text("Confirm Logout") },
                                text = { Text("Are you sure you want to log out?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showLogoutDialog = false
                                        firebaseHelper.signOut()
                                        AuthManager.updateAuthState()
                                        navController.navigate("login") {
                                            popUpTo("HomeScreen") { inclusive = true }
                                        }
                                    }) {
                                        Text("Yes")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showLogoutDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }

                }
            }

            // Define images for carousel
            val bannerImages = listOf(
                R.drawable.ic_banner_image,
                R.drawable.activtiy1,
                R.drawable.activtiy2,
                R.drawable.activtiy3,
                R.drawable.activtiy4
            )

            // Replace static banner with carousel
            BannerCarousel(
                imageResources = bannerImages
            ) {
                // Gradient overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
                
                // Banner content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Enjoy and Get Activities",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("activityList") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Get Now")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Improved Recent Activities section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate("activityList")
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        Text("Loading failure: $error", color = Color.Red)
                    }
                }
                activities.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No activities available at the moment")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(activities) { activity ->
                            ActivityCard(
                                activity = activity,
                                onActivityClick = { activityId ->
                                    navController.navigate("activity_details/$activityId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: Activity, onActivityClick: (String) -> Unit = {}) {
    val imageRes = when (activity.type) {
        "Movie" -> R.drawable.movie
        "Hiking" -> R.drawable.hiking
        else -> R.drawable.pic1
    }
    
    // Format time
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(activity.date)
    
    // Redesigned activity card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onActivityClick(activity.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Card top image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = activity.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Activity type label
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = activity.type ?: "Activity",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Card content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Date row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description text
                if (!activity.description.isNullOrEmpty()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Organizer
                Text(
                    text = "By ${activity.organizer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}