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
    
    var userData by remember { mutableStateOf<User?>(null) }
    var userPreferences by remember { mutableStateOf<UserPreferences?>(null) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarResId by remember { mutableStateOf(R.drawable.placeholder_avatar) }

    val allTypes = listOf("Hiking", "Biking", "Movies", "Poker")
    var selectedType by remember { mutableStateOf(allTypes.first()) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var area by remember { mutableStateOf("") }

    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val currentUser = firebaseHelper.getCurrentUser()
            if (currentUser != null) {
                firebaseHelper.getUserData(
                    uid = currentUser.uid,
                    onSuccess = { user ->
                        userData = user
                        name = user.username
                        email = user.email
                        birthday = user.birthday
                        isLoading = false
                    },
                    onError = { errorMsg ->
                        error = errorMsg
                        isLoading = false
                    }
                )
                
                firebaseHelper.getUserPreferences(
                    uid = currentUser.uid,
                    onSuccess = { preferences ->
                        userPreferences = preferences
                        if (preferences.activityType.isNotEmpty()) {
                            selectedType = preferences.activityType
                        }
                        area = preferences.activityArea
                    },
                    onError = { /* Handle errors and use the default values */ }
                )
            } else {
                navController.navigate("login")
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
            error = "The user ID cannot be obtainedD"
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
            age = userData?.age ?: ""
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
            activityArea = area
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
                    .padding(16.dp)
            ) {
                error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = avatarResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Gray, CircleShape)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(email, fontSize = 16.sp, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isEditing = !isEditing }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Edit Profile",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(if (isEditing) "Collapse" else "Expand", fontSize = 14.sp, color = Color.Gray)
                        }

                        if (isEditing) {
                            Spacer(Modifier.height(12.dp))
                            Column {
                                TextFieldWithLabel("Name", name) { name = it }
                                TextFieldWithLabel("Address", address) { address = it }
                                TextFieldWithLabel("Phone", phone) { phone = it }
                                BirthdayPickerField("Birthday", birthday) { showDatePicker() }
                                TextFieldWithLabel("Email", email) { email = it }
                                AvatarUploadField("Avatar") { avatarResId = R.drawable.placeholder_avatar2 }

                                Spacer(Modifier.height(12.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { isEditing = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                                    ) { Text("Cancel") }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = { saveUserProfile() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA))
                                    ) { Text("Save") }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPrefOpen = !isPrefOpen }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Preference Setting",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(if (isPrefOpen) "Collapse" else "Expand", fontSize = 14.sp, color = Color.Gray)
                        }

                        if (isPrefOpen) {
                            Spacer(Modifier.height(12.dp))
                            Column {

                                Text("Activity type", fontWeight = FontWeight.SemiBold)

                                ExposedDropdownMenuBox(
                                    expanded = typeDropdownExpanded,
                                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth())
                                {
                                    TextField(
                                        value = selectedType,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(typeDropdownExpanded)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { typeDropdownExpanded = !typeDropdownExpanded },
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

                                Spacer(Modifier.height(12.dp))
                                TextFieldWithLabel("Activity area", area) { area = it }

                                Spacer(Modifier.height(12.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { isPrefOpen = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                                    ) { Text("Cancel") }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = { savePreferences() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA))
                                    ) { Text("Save") }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { logout() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}


@Composable
fun TextFieldWithLabel(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
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
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
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
fun AvatarUploadField(label: String, onUploadClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Button(
            onClick = onUploadClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))
        ) {
            Text("Upload")
        }
    }
}