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
import kotlinx.coroutines.launch
import com.example.activitymanager.AppDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivityScreen(
    onNavigateBack: () -> Unit,
    onActivityCreated: (Activity) -> Unit,
) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var participantsIDs by remember { mutableStateOf<List<String>>(emptyList()) }
    var latitude by remember { mutableStateOf("0.0") }
    var longitude by remember { mutableStateOf("0.0") }
    var currentUserId by remember { mutableStateOf("") }

    var isTypeDropdownExpanded by remember { mutableStateOf(false) }

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

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState()
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
                title = { Text("Create a New Activity") },
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
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        duration = it
                    }
                },
                label = { Text("Duration (Minutes)") },
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
            val rating = (30..50).random() / 10.0
            Button(
                onClick = {
                    val newActivity = Activity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        date = parseDateTime(date, time),
                        location = location,
                        organizer = organizer,
                        uid = currentUserId,
                        rating = rating,
                        type = type,
                        duration = duration,
                        participants = participants.toIntOrNull() ?: 0,
                        participantsIDs = participantsIDs,
                        isFavorite = false,
                        coordinates = LatLng(
                            latitude.toDoubleOrNull() ?: 0.0,
                            longitude.toDoubleOrNull() ?: 0.0
                        )
                    )
                    coroutineScope.launch {
                        firebaseHelper.createActivity(
                            activity = newActivity,
                            onSuccess = {
                                Toast.makeText(context, "Activity posted successfully", Toast.LENGTH_SHORT).show()
                                onActivityCreated(newActivity)
                                onNavigateBack()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    // onActivityCreated(newActivity)
                    // onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            ) {
                Text("Post")
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
