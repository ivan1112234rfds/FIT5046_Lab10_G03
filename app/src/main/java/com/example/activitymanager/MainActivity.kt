package com.example.activitymanager

import CreateActivityScreen
import EditActivityScreen
import android.annotation.SuppressLint
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.firebase.AuthManager
import com.example.activitymanager.mapper.Activity
import com.example.activitymanager.ui.theme.ActivityManagerTheme
import com.example.activitymanager.LoginScreen
import com.example.activitymanager.RegisterScreen
import com.example.activitymanager.HomeScreen
import com.example.activitymanager.ProfileScreen
import com.example.activitymanager.ui.ProtectedRoute
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        
        AuthManager.initialize()
        
        // Initialize Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized successfully")
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
            
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
                        AuthManager.updateAuthState()
                        showToast("Google Sign-In successful")
                    },
                    onError = { error ->
                        showToast("Google Sign-In failed: $error")
                    }
                )
            }
        }
        
        setContent {
            ActivityManagerTheme {
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
fun navigateToTab(navController: NavController, route: String) {
    val navigationRoute = when (route) {
        "Home" -> "home"
        "My Activity" -> "activities"
        "Search" -> "search"
        "Manage" -> "manage"
        "Profile" -> "profile"
        else -> route
    }
    navController.navigate(navigationRoute) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityApp(
    firebaseHelper: FirebaseHelper,
    onGoogleSignIn: () -> Unit
) {
    val navController = rememberNavController()
    val isLoggedIn by AuthManager.isLoggedIn

    var showLoginDialog by remember { mutableStateOf(false) }
    var loginRedirectTarget by remember { mutableStateOf<String?>(null) }

    NavHost(navController = navController, startDestination = "Home") {
        composable("activities") {
            ProtectedRoute(
                isLoggedIn = isLoggedIn,
                onNotLoggedIn = {
                    navController.popBackStack()
                    navController.navigate("Home")
                }
            ) {
                MyActivitiesScreen(
                    navController = navController,
                    onActivityClick = { activityId ->
                        navController.navigate("activity_details/$activityId")
                    }
                )
            }
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
            ProtectedRoute(
                isLoggedIn = isLoggedIn,
                onNotLoggedIn = {
                    navController.popBackStack()
                    navController.navigate("Home")
                }
            ) {
                CreateActivityScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onActivityCreated = {}
                )
            }
        }
        composable("activityList") { 
            ActivityScreen(navController, onActivityClick = { activityId ->
                navController.navigate("activity_details/$activityId")
            })
        }
        composable("home") {
            HomeScreen(navController)
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
            ProtectedRoute(
                isLoggedIn = isLoggedIn,
                onNotLoggedIn = {
                    navController.popBackStack()
                    navController.navigate("Home")
                }
            ) {
                ActivityManageScreen(navController)
            }
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
            ProtectedRoute(
                isLoggedIn = isLoggedIn,
                onNotLoggedIn = {
                    navController.popBackStack()
                    navController.navigate("Home")
                }
            ) {
                EditActivityScreen(
                    navController,
                    onNavigateBack = { navController.popBackStack() },
                    onActivityCreated = { }
                )
            }
        }
    }
    
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login Required") },
            text = { Text("You need to be logged in to access this feature.") },
            confirmButton = {
                Button(onClick = { 
                    showLoginDialog = false
                    navController.navigate("login")
                }) {
                    Text("Login")
                }
            },
            dismissButton = {
                Button(onClick = { 
                    showLoginDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
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
            participantsIDs = emptyList(),
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
            participantsIDs = emptyList(),
            type = "Hiking",
            coordinates = LatLng(0.0, 0.0)
        )
    )
    return mockActivities
}

