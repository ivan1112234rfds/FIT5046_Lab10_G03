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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.mapper.Activity
import com.google.firebase.auth.FirebaseAuth
import java.text.ParseException
import com.example.activitymanager.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyActivitiesScreen(
    navController: NavController,
    onActivityClick: (String) -> Unit
) {
    val firebaseHelper = remember { FirebaseHelper() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userId = currentUser?.uid ?: ""

    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf("My Activity") }
    val tabs = listOf("Upcoming", "Past", "All")

    LaunchedEffect(Unit) {
        selectedTab = "My Activity"
    }

    LaunchedEffect(userId) {
        isLoading = true
        try {
            val allActivities = firebaseHelper.getActivities()
            val userActivities = allActivities.filter { activity ->
                activity.participantsIDs?.contains(userId) == true
            }
            activities = userActivities
            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load activities: ${e.message}"
            isLoading = false
        }
    }

    val parseDate = { dateString: String? ->
        if (dateString == null) null
        else try {
            val format = SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss z", Locale.US)
            format.parse(dateString)
        } catch (e1: ParseException) {
            try {
                val format = SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss 'UTC'Z", Locale.US)
                format.parse(dateString)
            } catch (e2: ParseException) {
                println("Cannot parse date: $dateString - ${e2.message}")
                null
            }
        }
    }

    val filteredActivities = when (selectedTabIndex) {
        0 -> activities.filter {
            val date = parseDate(it.date.toString())
            date?.after(Date()) ?: true
        }
        1 -> activities.filter {
            val date = parseDate(it.date.toString())
            date?.before(Date()) ?: false
        }
        else -> activities
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "My Activities",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "Unknown error", color = Color.Red)
                    }
                }
                filteredActivities.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No activities found")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredActivities) { activity ->
                            ActivityItem(
                                activity = activity,
                                onClick = { onActivityClick(activity.id) },
                                parseDateFunction = parseDate
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItem(
    activity: Activity,
    onClick: () -> Unit,
    parseDateFunction: (String?) -> Date? = {
        dateString ->
            if (dateString == null) null
            else try {
                val format = SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss z", Locale.US)
                format.parse(dateString)
            } catch (e1: ParseException) {
                try {
                    val format = SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss 'UTC'Z", Locale.US)
                    format.parse(dateString)
                } catch (e2: ParseException) {
                    null
                }
            }

    }
) {
    val imageRes = when (activity.type?.lowercase() ?: "") {
        "movie" -> R.drawable.movie
        "hiking" -> R.drawable.hiking
        else -> R.drawable.pic1
    }

    val rating = activity.rating ?: 0.0

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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = activity.title ?: "No Title", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "by ${activity.organizer ?: "Unknown"}",
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
                        text = "$rating ${activity.duration ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                val actualParticipants = activity.participantsIDs?.size ?: 0
                Text(
                    text = "$actualParticipants participants",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                val isFavorite = activity.isFavorite ?: false
                IconButton(
                    onClick = {  },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.secondary else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val date = parseDateFunction(activity.date.toString())
                if (date != null) {
                    val outputFormat = SimpleDateFormat("MMM d", Locale.US)
                    Text(
                        text = outputFormat.format(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}