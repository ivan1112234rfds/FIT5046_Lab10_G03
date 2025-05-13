package com.example.activitymanager

import CreateActivityScreen
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val firebaseHelper = FirebaseHelper()
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化 Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        
        // 初始化 Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }
        
        // 初始化 Google 登录
        val webClientId = getString(R.string.web_client_id)
        firebaseHelper.initGoogleSignIn(this, webClientId)
        
        // 注册 Google 登录结果处理
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            lifecycleScope.launch {
                firebaseHelper.handleGoogleSignInResult(
                    data = result.data,
                    onSuccess = {
                        // 登录成功后导航到主页
                        // 由于这里不能直接使用 navController，我们可以通过设置一个标志来处理
                        showToast("Google 登录成功")
                    },
                    onError = { error ->
                        showToast("Google 登录失败: $error")
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
            showToast("Google 登录初始化失败")
        }
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
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

