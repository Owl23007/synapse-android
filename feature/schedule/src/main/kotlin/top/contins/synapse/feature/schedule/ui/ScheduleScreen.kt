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
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
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
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val selectedDateSchedules by viewModel.selectedDateSchedules.collectAsState()
    val calendars by viewModel.calendars.collectAsState()
    var viewType by remember { mutableStateOf(CalendarViewType.MONTH) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // View Switcher
        CalendarViewTabs(
            selectedViewType = viewType,
            onViewTypeSelected = { viewType = it }
        )

        when (viewType) {
            CalendarViewType.MONTH -> MonthView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                schedules = schedules,
                onDateSelected = viewModel::onDateSelected,
                onMonthChanged = viewModel::onMonthChanged
            )
            CalendarViewType.WEEK -> Text("周视图 (即将推出)", modifier = Modifier.padding(16.dp))
            CalendarViewType.DAY -> Text("日视图 (即将推出)", modifier = Modifier.padding(16.dp))
        }
        
        // Schedule List for selected date
        if (viewType == CalendarViewType.MONTH) {
            ScheduleList(schedules = selectedDateSchedules)
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            selectedDate = selectedDate,
            calendars = calendars,
            onDismiss = { showAddDialog = false },
            onConfirm = { schedule ->
                viewModel.createSchedule(schedule)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ScheduleList(schedules: List<Schedule>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(schedules.size) { index ->
            ScheduleItem(schedule = schedules[index])
        }
    }
}

@Composable
fun ScheduleItem(schedule: Schedule) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    }
}

fun formatTime(timestamp: Long): String {
    val instant = java.time.Instant.ofEpochMilli(timestamp)
    val zoneId = java.time.ZoneId.systemDefault()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    return instant.atZone(zoneId).format(formatter)
}