import com.example.activitymanager.Activity
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
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivityScreen(
    onNavigateBack: () -> Unit,
    onActivityCreated: (Activity) -> Unit
) {
    // 状态管理
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("0.0") }
    var longitude by remember { mutableStateOf("0.0") }
  
    // 日期选择器状态
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // 时间选择器状态
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    // 表单验证
    val isFormValid = title.isNotBlank() && description.isNotBlank() &&
            location.isNotBlank() && date.isNotBlank() && time.isNotBlank() &&
            organizer.isNotBlank() && duration.isNotBlank()

    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // 格式化选中的日期
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Date(it)
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        date = formatter.format(selectedDate)
                    }
                    showDatePicker = false
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

    // 时间选择器对话框
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choose Time") },
            confirmButton = {
                TextButton(onClick = {
                    // 格式化选中的时间
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Activity Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
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

            // 日期选择
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Choose a Date")
                    }
                }
            )

            // 时间选择
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Choose Time")
                    }
                }
            )

            // 组织者输入
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

            // 活动时长
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

            // 参与人数
            OutlinedTextField(
                value = participants,
                onValueChange = {
                    // 只允许输入数字
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

            Spacer(modifier = Modifier.height(16.dp))

            // 发布按钮
            Button(
                onClick = {
                    // 创建活动对象并回调
                    val newActivity = Activity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        date = parseDateTime(date, time),
                        location = location,
                        organizer = organizer,
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
                    onActivityCreated(newActivity)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Post")
            }
        }
    }
}

// 辅助函数：将日期和时间字符串解析为Date对象
private fun parseDateTime(dateStr: String, timeStr: String): Date {
    val dateTimeStr = "$dateStr $timeStr"
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return try {
        formatter.parse(dateTimeStr) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}
