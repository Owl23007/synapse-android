package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.feature.schedule.viewmodel.ScheduleViewModel
import top.contins.synapse.domain.model.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    showFab: Boolean = true,
    addTick: Int = 0
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val selectedDateSchedules by viewModel.selectedDateSchedules.collectAsState()
    val calendars by viewModel.calendars.collectAsState()
    var viewType by remember { mutableStateOf(CalendarViewType.MONTH) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(addTick) {
        if (addTick > 0) {
            showAddDialog = true
        }
    }

    val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // View Switcher
            CalendarViewTabs(
                selectedViewType = viewType,
                onViewTypeSelected = { viewType = it }
            )

            when (viewType) {
                CalendarViewType.MONTH -> {
                    MonthView(
                        modifier = Modifier.fillMaxWidth(),
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        schedules = schedules,
                        onDateSelected = viewModel::onDateSelected,
                        onMonthChanged = viewModel::onMonthChanged
                    )

                    // 月视图下面显示日程（选中日期）
                    ScheduleList(
                        modifier = Modifier.weight(1f),
                        schedules = selectedDateSchedules,
                        onDelete = viewModel::deleteSchedule
                    )
                }

                CalendarViewType.WEEK -> Text("周视图 (即将推出)", modifier = Modifier.padding(16.dp))
                CalendarViewType.DAY -> Text("日视图 (即将推出)", modifier = Modifier.padding(16.dp))
            }
        }
    }

    if (showFab) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Schedule")
                }
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    } else {
        content(PaddingValues(0.dp))
    }

    if (showAddDialog) {
        AddScheduleDialog(
            initialDate = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            onDismiss = { showAddDialog = false },
            onConfirm = { title, startTime, endTime, location, isAllDay, reminderMinutes, repeatRule ->
                val currentTime = System.currentTimeMillis()
                val schedule = Schedule(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    startTime = startTime,
                    endTime = endTime,
                    timezoneId = java.util.TimeZone.getDefault().id,
                    location = location.ifBlank { null },
                    type = top.contins.synapse.domain.model.ScheduleType.EVENT,
                    calendarId = "default",
                    isAllDay = isAllDay,
                    reminderMinutes = reminderMinutes,
                    repeatRule = repeatRule,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                viewModel.createSchedule(schedule)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ScheduleList(
    modifier: Modifier = Modifier,
    schedules: List<Schedule>,
    onDelete: (Schedule) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(schedules.size) { index ->
            ScheduleItem(schedule = schedules[index], onDelete = onDelete)
        }
    }
}

@Composable
fun ScheduleItem(schedule: Schedule, onDelete: (Schedule) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = schedule.title, style = MaterialTheme.typography.titleMedium)
                schedule.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                // Format time properly
                Text(
                    text = "${formatTime(schedule.startTime)} - ${formatTime(schedule.endTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { onDelete(schedule) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Schedule")
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val instant = java.time.Instant.ofEpochMilli(timestamp)
    val zoneId = java.time.ZoneId.systemDefault()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    return instant.atZone(zoneId).format(formatter)
}