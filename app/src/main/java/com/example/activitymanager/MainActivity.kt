package com.example.activitymanager

import CreateActivityScreen
import EditActivityScreen
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Date
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.mapper.Activity
import com.example.assignmentcode.ui.theme.AssignmentCodeTheme
import com.example.activitymanager.LoginScreen
import com.example.activitymanager.RegisterScreen
import com.example.fit5046assignment.HomeScreen
import com.example.fit5046assignment.ProfileScreen
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val firebaseHelper = FirebaseHelper()
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized successfully")
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
            
            // 移除emulator连接，我们将在LoginScreen中处理
            // useEmulator = false 保留注释以提醒未来可能的变更
            /*
            val useEmulator = false
            if (useEmulator) {
                try {
                    Firebase.auth.useEmulator("10.0.2.2", 9099)
                    Log.d(TAG, "Using Auth Emulator on 10.0.2.2:9099")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to connect to Auth Emulator", e)
                }
            }
            */
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
            showToast("Failed to initialize Firebase: ${e.message}")
        }
        
        // Initialize Places API
        try {
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, getString(R.string.google_maps_key))
                Log.d(TAG, "Places API initialized successfully")
            } else {
                Log.d(TAG, "Places API already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Places API", e)
            showToast("Failed to initialize Places API: ${e.message}")
        }
        
        // Initialize Google Sign-In
        try {
            val webClientId = getString(R.string.web_client_id)
            firebaseHelper.initGoogleSignIn(this, webClientId)
            Log.d(TAG, "Google Sign-In initialized with webClientId: $webClientId")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Google Sign-In", e)
            showToast("Failed to initialize Google Sign-In: ${e.message}")
        }
        
        // Register Google Sign-In result handler
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            lifecycleScope.launch {
                firebaseHelper.handleGoogleSignInResult(
                    data = result.data,
                    onSuccess = {
                        // Navigate to home page after successful login
                        // Since we can't directly use navController here, we can handle it by setting a flag
                        showToast("Google Sign-In successful")
                    },
                    onError = { error ->
                        showToast("Google Sign-In failed: $error")
                    }
                )
            }
        }
        
        setContent {
            AssignmentCodeTheme {
                ActivityApp(
                    firebaseHelper = firebaseHelper,
                    onGoogleSignIn = { startGoogleSignIn() }
                )
            }
        }
    }
    
    private fun startGoogleSignIn() {
        val signInIntent = firebaseHelper.getGoogleSignInIntent()
        if (signInIntent != null) {
            googleSignInLauncher.launch(signInIntent)
        } else {
            showToast("Google Sign-In initialization failed")
        }
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun ActivityApp(
    firebaseHelper: FirebaseHelper,
    onGoogleSignIn: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Home") {
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
                onActivityCreated = {
                }
            )
        }
        composable("activityList") { ActivityScreen(navController, onActivityClick = { activityId ->
            navController.navigate("activity_details/$activityId")
        },) }
        composable("home") {
            HomeScreens(navController)
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                onGoogleSignIn = onGoogleSignIn
            )
        }

        composable("register") {
            com.example.activitymanager.RegisterScreen(navController)
        }
        composable("Manage") {
            ActivityManageScreen(navController)
        }
        composable("Dashboard") {
            DashboardScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable("HomeScreen") {
            HomeScreen(navController)
        }
        composable("Profile") {
            ProfileScreen(navController)
        }
        composable("edit_activity") {
            EditActivityScreen(
                navController,
                onNavigateBack = { navController.popBackStack() },
                onActivityCreated = { }
            )
        }

    }
}






fun createMockActivities(): List<Activity> {
    val cal = Calendar.getInstance()
    val mockActivities = mutableListOf<Activity>()

    cal.add(Calendar.DAY_OF_YEAR, 1)
    mockActivities.add(
        Activity(
            id = "1",
            title = "Art Masterclass",
            description = "Join our comprehensive Art Masterclass.",
            date = cal.time,
            location = "Art Studio 42, 123 Creative Avenue, San Francisco",
            organizer = "Roy Marsh",
            uid = "",
            rating = 4.9,
            duration = "2h 30mins",
            participants = 1245,
            type = "Hiking",
            isFavorite = true,
            coordinates = LatLng(37.7749, -122.4194)  // San Francisco coordinates
        )
    )

    cal.add(Calendar.DAY_OF_YEAR, 2)
    mockActivities.add(
        Activity(
            id = "2",
            title = "Figma Mastery",
            description = "Become a Figma expert with this comprehensive course.",
            date = cal.time,
            location = "Online",
            organizer = "Annie Chandler",
            uid = "",
            rating = 4.2,
            duration = "3h 30mins",
            participants = 2182,
            type = "Hiking",
            coordinates = LatLng(0.0, 0.0)  // Online course
        )
    )
    return mockActivities
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
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFF6366F1) else Color.Gray
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (selected) Color(0xFF6366F1) else Color.Gray
        )
    }
}

