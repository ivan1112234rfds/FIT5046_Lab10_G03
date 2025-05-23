package com.example.activitymanager

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.activitymanager.R
import com.example.activitymanager.firebase.FirebaseHelper
import com.example.activitymanager.firebase.AuthManager
import com.example.activitymanager.model.User
import com.example.activitymanager.model.UserPreferences
import com.example.activitymanager.BottomNavigationBar
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.activitymanager.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.LifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var isPrefOpen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Profile") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var quote by remember { mutableStateOf<String?>(null) }
    var isLoadingQuote by remember { mutableStateOf(false) }
    val quoteService = remember { ZenQuoteService() }
    
    var userData by remember { mutableStateOf<User?>(null) }
    var userPreferences by remember { mutableStateOf<UserPreferences?>(null) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarResId by remember { mutableStateOf(R.drawable.placeholder_avatar) }

    var allTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedType by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    
    val isLoggedIn by AuthManager.isLoggedIn
    val currentAuthUser by AuthManager.currentUser

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            isLoadingQuote = true
            try {
                quote = quoteService.fetchQuoteText()
            } catch (e: Exception) {
                quote = null
            } finally {
                isLoadingQuote = false
            }
        } else {
            quote = null
        }
    }

    LaunchedEffect(isLoggedIn) {
        isLoading = true
        try {
            if (isLoggedIn && currentAuthUser != null) {
                val user = currentAuthUser
                val uid = user?.uid ?: ""
                
                if (uid.isNotEmpty()) {
                    firebaseHelper.getUserData(
                        uid = uid,
                        onSuccess = { user ->
                            userData = user
                            name = user.username
                            email = user.email
                            birthday = user.birthday
                            address = user.address
                            phone = user.phone
                            isLoading = false
                        },
                        onError = { errorMsg ->
                            error = errorMsg
                            isLoading = false
                        }
                    )
                    
                    firebaseHelper.getUserPreferences(
                        uid = uid,
                        onSuccess = { preferences ->
                            userPreferences = preferences
                            if (preferences.activityType.isNotEmpty()) {
                                selectedType = preferences.activityType
                            }
                        },
                        onError = { /* Handle errors and use the default values */ }
                    )
                    
                    try {
                        allTypes = firebaseHelper.getActivityTypes()
                        if (allTypes.isEmpty()) {
                            allTypes = listOf("Hiking", "Biking", "Movies", "Poker")
                        }
                    } catch (e: Exception) {
                        allTypes = listOf("Hiking", "Biking", "Movies", "Poker")
                    }
                }
            } else {
                userData = null
                userPreferences = null
                isLoading = false
            }
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (birthday.isNotEmpty()) {
            try {
                val parts = birthday.split("-")
                if (parts.size == 3) {
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (e: Exception) {
            }
        }
        
        DatePickerDialog(
            context,
            { _, y, m, d -> birthday = "$y-${m + 1}-$d" },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    fun saveUserProfile() {
        isLoading = true
        val uid = userData?.uid ?: firebaseHelper.getCurrentUser()?.uid ?: ""
        
        if (uid.isEmpty()) {
            error = "The user ID cannot be obtained"
            isLoading = false
            return
        }
        
        val updatedUser = User(
            uid = uid,
            username = name,
            nickname = name,
            email = email,
            birthday = birthday,
            gender = userData?.gender ?: "",
            age = userData?.age ?: "",
            address = address,
            phone = phone
        )
        
        val scope = (context as LifecycleOwner).lifecycleScope
        scope.launch {
            try {
                firebaseHelper.updateUserData(
                    user = updatedUser,
                    onSuccess = {
                        userData = updatedUser
                        isLoading = false
                        isEditing = false
                    },
                    onError = { errorMessage ->
                        error = errorMessage
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }
    
    fun savePreferences() {
        isLoading = true
        val uid = userData?.uid ?: firebaseHelper.getCurrentUser()?.uid ?: ""
        
        if (uid.isEmpty()) {
            error = "The user ID cannot be obtained"
            isLoading = false
            return
        }
        
        val updatedPreferences = UserPreferences(
            uid = uid,
            activityType = selectedType,
            activityArea = ""
        )
        
        val scope = (context as LifecycleOwner).lifecycleScope
        scope.launch {
            try {
                firebaseHelper.updateUserPreferences(
                    preferences = updatedPreferences,
                    onSuccess = {
                        userPreferences = updatedPreferences
                        isLoading = false
                        isPrefOpen = false
                    },
                    onError = { errorMessage ->
                        error = errorMessage
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }
    
    fun logout() {
        firebaseHelper.signOut()
        AuthManager.updateAuthState()

        val scope = (context as LifecycleOwner).lifecycleScope
        scope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).clearAllTables()
            }
            navController.navigate("login") {
                popUpTo("Profile") { inclusive = true }
            }
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isLoggedIn) avatarResId 
                                else R.drawable.no_login
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLoggedIn) userData?.username ?: "User" else "Not Logged In",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isLoggedIn && email.isNotEmpty()) {
                    Text(
                        text = email,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                if (isLoggedIn && quote != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DailyQuoteCard(quote = quote!!)
                } else if (isLoggedIn && isLoadingQuote) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isLoggedIn) {
                                        isEditing = !isEditing
                                    } else {
                                        navController.navigate("login")
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit Profile",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (isEditing) "Collapse" else "Expand",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isLoggedIn) {
                                        isPrefOpen = !isPrefOpen
                                    } else {
                                        navController.navigate("login")
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_settings),
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Preference Setting",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (isPrefOpen) "Collapse" else "Expand",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        if (isLoggedIn) {
                            logout()
                        } else {
                            navController.navigate("login")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoggedIn) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLoggedIn) "Logout" else "Login",
                        fontSize = 16.sp
                    )
                }
            }
        }

        if (isEditing && isLoggedIn) {
            AlertDialog(
                onDismissRequest = { isEditing = false },
                title = { Text("Edit Profile") },
                text = {
                    Column {
                        TextFieldWithLabel("Name", name) { name = it }
                        TextFieldWithLabel("Address", address) { address = it }
                        TextFieldWithLabel("Phone", phone) { phone = it }
                        BirthdayPickerField("Birthday", birthday) { showDatePicker() }
                        ReadOnlyFieldWithLabel("Email", email)
                        AvatarUploadField("Avatar") { 
                            when (it) {
                                1 -> avatarResId = R.drawable.placeholder_avatar
                                2 -> avatarResId = R.drawable.placeholder_avatar2
                                3 -> avatarResId = R.drawable.placeholder_avatar3
                                else -> avatarResId = R.drawable.placeholder_avatar
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { saveUserProfile() }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { isEditing = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (isPrefOpen && isLoggedIn) {
            AlertDialog(
                onDismissRequest = { isPrefOpen = false },
                title = { Text("Preference Settings") },
                text = {
                    Column {
                        Text("Activity type", fontWeight = FontWeight.SemiBold)
                        ExposedDropdownMenuBox(
                            expanded = typeDropdownExpanded,
                            onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = selectedType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(typeDropdownExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF3F4F6),
                                    disabledContainerColor = Color(0xFFF3F4F6)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                allTypes.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = {
                                            selectedType = t
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { savePreferences() }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { isPrefOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TextFieldWithLabel(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF3F4F6),
                disabledContainerColor = Color(0xFFF3F4F6)
            )
        )
    }
}

@Composable
fun BirthdayPickerField(label: String, birthday: String, onPickDate: () -> Unit) {
    Column(Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = birthday,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPickDate() },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Black,
                disabledLabelColor = Color.Gray,
                disabledLeadingIconColor = Color.Gray,
                disabledPlaceholderColor = Color.LightGray,
                unfocusedContainerColor = Color(0xFFF3F4F6),
                disabledContainerColor = Color(0xFFF3F4F6)
            )
        )
    }
}

@Composable
fun AvatarUploadField(label: String, onUploadClick: (Int) -> Unit) {
    Column(Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        
        var isAvatarDialogVisible by remember { mutableStateOf(false) }
        
        Button(
            onClick = { isAvatarDialogVisible = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF3F4F6),
                contentColor = Color(0xFF3C4043)
            )
        ) {
            Text("Choose Avatar")
        }
        
        if (isAvatarDialogVisible) {
            AlertDialog(
                onDismissRequest = { isAvatarDialogVisible = false },
                title = { Text("Select Avatar") },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1st Avatar option
                        Image(
                            painter = painterResource(id = R.drawable.placeholder_avatar),
                            contentDescription = "Avatar 1",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onUploadClick(1)
                                    isAvatarDialogVisible = false
                                },
                            contentScale = ContentScale.Crop
                        )
                        
                        // 2nd Avatar option
                        Image(
                            painter = painterResource(id = R.drawable.placeholder_avatar2),
                            contentDescription = "Avatar 2",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onUploadClick(2)
                                    isAvatarDialogVisible = false
                                },
                            contentScale = ContentScale.Crop
                        )
                        
                        // 3rd Avatar option
                        Image(
                            painter = painterResource(id = R.drawable.placeholder_avatar3),
                            contentDescription = "Avatar 3",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onUploadClick(3)
                                    isAvatarDialogVisible = false
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { isAvatarDialogVisible = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DailyQuoteCard(quote: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Inspiration",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ReadOnlyFieldWithLabel(label: String, value: String) {
    Column(Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        TextField(
            value = value,
            onValueChange = { /* Read-only, no changes allowed */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = TextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color(0xFFF3F4F6)
            )
        )
    }
}