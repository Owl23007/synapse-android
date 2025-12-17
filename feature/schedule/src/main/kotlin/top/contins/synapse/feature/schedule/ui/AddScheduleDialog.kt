package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import top.contins.synapse.domain.model.CalendarAccount
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.model.ScheduleType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    selectedDate: LocalDate,
    calendars: List<CalendarAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Schedule) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(selectedDate) }
    var startTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var endDate by remember { mutableStateOf(selectedDate) }
    var endTime by remember { mutableStateOf(LocalTime.now().plusHours(1).withSecond(0).withNano(0)) }
    var isAllDay by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(ScheduleType.PERSONAL) }
    var selectedCalendarId by remember { mutableStateOf(calendars.firstOrNull()?.id ?: "") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Text("新建日程", style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = {
                            val zoneId = ZoneId.systemDefault()
                            val startEpoch = startDate.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli()
                            val endEpoch = endDate.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli()
                            
                            val newSchedule = Schedule(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                startTime = startEpoch,
                                endTime = endEpoch,
                                timezoneId = zoneId.id,
                                type = selectedType,
                                calendarId = selectedCalendarId,
                                isAllDay = isAllDay,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onConfirm(newSchedule)
                        },
                        enabled = title.isNotBlank() && selectedCalendarId.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("描述") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("全天")
                        Switch(checked = isAllDay, onCheckedChange = { isAllDay = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("开始")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { showStartDatePicker = true }) {
                                Text(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            }
                            if (!isAllDay) {
                                TextButton(onClick = { showStartTimePicker = true }) {
                                    Text(startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("结束")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { showEndDatePicker = true }) {
                                Text(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            }
                            if (!isAllDay) {
                                TextButton(onClick = { showEndTimePicker = true }) {
                                    Text(endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                                }
                            }
                        }
                    }

                    if (calendars.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = calendars.find { it.id == selectedCalendarId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("日历") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                calendars.forEach { calendar ->
                                    DropdownMenuItem(
                                        text = { Text(calendar.name) },
                                        onClick = {
                                            selectedCalendarId = calendar.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text("类型")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScheduleType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.displayName) }
                            )
                        }
                    }
                }
            }
        }

        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showStartTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = startTime.hour,
                initialMinute = startTime.minute
            )
            TimePickerDialog(
                onDismissRequest = { showStartTimePicker = false },
                onConfirm = {
                    startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showStartTimePicker = false
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }

        if (showEndTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = endTime.hour,
                initialMinute = endTime.minute
            )
            TimePickerDialog(
                onDismissRequest = { showEndTimePicker = false },
                onConfirm = {
                    endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showEndTimePicker = false
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("取消") }
        },
        text = content
    )
}
