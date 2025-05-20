import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.activitymanager.mapper.Activity
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.activitymanager.firebase.FirebaseHelper
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.activitymanager.AppDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onActivityCreated: (Activity) -> Unit,
) {

    val backStack = navController.previousBackStackEntry?.savedStateHandle

    val id by remember { mutableStateOf(backStack?.get<String>("id") ?: "") }
    var title by remember { mutableStateOf(backStack?.get<String>("title") ?: "") }
    var description by remember { mutableStateOf(backStack?.get<String>("description") ?: "") }
    var location by remember { mutableStateOf(backStack?.get<String>("location") ?: "") }
    val rawDateTime = backStack?.get<String>("date") ?: ""
    var duration by remember { mutableStateOf(backStack?.get<String>("duration") ?: "") }
    var organizer by remember { mutableStateOf(backStack?.get<String>("organizer") ?: "") }
    var type by remember { mutableStateOf(backStack?.get<String>("type") ?: "") }
    var participants by remember { mutableStateOf(backStack?.get<String>("participants") ?: "") }
    val coordinatesMap = backStack?.get<Map<String, Any>>("coordinates")
    var latitude by remember { mutableStateOf((coordinatesMap?.get("latitude") as? Double)?.toString() ?: "0.0") }
    var longitude by remember { mutableStateOf((coordinatesMap?.get("longitude") as? Double)?.toString() ?: "0.0") }
    var currentUserId by remember { mutableStateOf("") }

    var isTypeDropdownExpanded by remember { mutableStateOf(false) }

    val parsedDate = try {
        SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss z", Locale.ENGLISH).parse(rawDateTime)
    } catch (e: Exception) {
        null
    }

    var date by remember {
        mutableStateOf(
            parsedDate?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            } ?: ""
        )
    }

    var time by remember {
        mutableStateOf(
            parsedDate?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: ""
        )
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parsedDate?.time
    )

    val calendar = Calendar.getInstance()
    calendar.time = parsedDate ?: Date()

    val timePickerState = remember {
        TimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )
    }



    val context = LocalContext.current
    val firebaseHelper = remember { FirebaseHelper() }
    var activityTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    val userDao = AppDatabase.getInstance(context).userDao()

    LaunchedEffect(Unit) {
        val result = firebaseHelper.getActivityTypes()
        if (result.isNotEmpty()) {
            activityTypes = result
        } else {
            Toast.makeText(context, "Failed to load activity types", Toast.LENGTH_SHORT).show()
        }

        // get current user's id
        val user = userDao.getCurrentUser()
        currentUserId = user?.uid ?: ""

        // get current username
        val userInfo = firebaseHelper.getUserinfoByUid(currentUserId)
        organizer = userInfo?.username ?: ""
    }

    // val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    val isFormValid = title.isNotBlank() && description.isNotBlank() &&
            location.isNotBlank() && date.isNotBlank() && time.isNotBlank() &&
            organizer.isNotBlank() && duration.isNotBlank()

    // select the date after today
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Date(it)

                        // Get the time at 00:00 tomorrow
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        val tomorrow = calendar.time

                        if (selectedDate.before(tomorrow)) {
                            // The date is less than tomorrow, indicating an error
                            Toast.makeText(
                                context,
                                "Please choose a date after today",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            date = formatter.format(selectedDate)
                            showDatePicker = false
                        }
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }




    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choose Time") },
            confirmButton = {
                TextButton(onClick = {

                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    time = String.format("%02d:%02d", hour, minute)
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Activity Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Activity Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            PlacesAutocompleteTextField(
                value = location,
                onValueChange = { location = it },
                onLocationSelected = { address, latLng ->
                    location = address
                    latitude = latLng.latitude.toString()
                    longitude = latLng.longitude.toString()
                },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(
                expanded = isTypeDropdownExpanded,
                onExpandedChange = { isTypeDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isTypeDropdownExpanded,
                    onDismissRequest = { isTypeDropdownExpanded = false }
                ) {
                    activityTypes.forEach { activityType ->
                        DropdownMenuItem(
                            text = { Text(activityType) },
                            onClick = {
                                type = activityType
                                isTypeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Choose a Date")
                    }
                }
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Choose Time")
                    }
                }
            )

            OutlinedTextField(
                value = organizer,
                onValueChange = { organizer = it },
                label = { Text("Organizer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = participants,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        participants = it
                    }
                },
                label = { Text("Participants Limit") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    val newActivity = Activity(
                        id = id,
                        title = title,
                        description = description,
                        date = parseDateTime(date, time),
                        location = location,
                        organizer = organizer,
                        uid = currentUserId,
                        rating = 0.0,
                        type = type,
                        duration = duration,
                        participants = participants.toIntOrNull() ?: 0,
                        isFavorite = false,
                        coordinates = LatLng(
                            latitude.toDoubleOrNull() ?: 0.0,
                            longitude.toDoubleOrNull() ?: 0.0
                        )
                    )
                    coroutineScope.launch {
                        firebaseHelper.updateActivityByFieldId(
                            activity = newActivity,
                            onSuccess = {
                                Toast.makeText(context, "Edit activity successfully", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            ) {
                Text("Edit")
            }
        }
    }
}

private fun parseDateTime(dateStr: String, timeStr: String): Date {
    val dateTimeStr = "$dateStr $timeStr"
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return try {
        formatter.parse(dateTimeStr) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}