package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.*

data class ScheduleEvent(
    val id: String,
    val title: String,
    val description: String,
    val startTime: Date,
    val endTime: Date,
    val type: EventType = EventType.MEETING,
    val color: Color = Color.Blue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val today = Date()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var viewType by remember { mutableStateOf(CalendarViewType.MONTH) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var events by remember {
        mutableStateOf(
            listOf(
                ScheduleEvent(
                    "1", "团队会议", "讨论项目进展",
                    Date(today.time + 2 * 60 * 60 * 1000), // 2小时后
                    Date(today.time + 3 * 60 * 60 * 1000), // 3小时后
                    EventType.MEETING
                ),
                ScheduleEvent(
                    "2", "学习Compose", "深入学习Jetpack Compose",
                    Date(today.time + 4 * 60 * 60 * 1000), // 4小时后
                    Date(today.time + 6 * 60 * 60 * 1000), // 6小时后
                    EventType.STUDY
                ),
                ScheduleEvent(
                    "3", "健身", "跑步锻炼",
                    Date(today.time + 8 * 60 * 60 * 1000), // 8小时后
                    Date(today.time + 9 * 60 * 60 * 1000), // 9小时后
                    EventType.PERSONAL
                )
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* new schedule */ }) {
                Icon(Icons.Default.Add, contentDescription = "添加日程")
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
        // 日历视图
        CalendarComponent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            currentDate = selectedDate,
            viewType = viewType,
            onDateSelected = { date ->
                selectedDate = date
                // 这里可以根据日期筛选事件
            },
            onViewTypeChanged = { newViewType ->
                viewType = newViewType
            },
            selectedDateTime = selectedDateTime,
            onTimeSlotClick = { date, time ->
                selectedDate = date
                selectedDateTime = LocalDateTime.of(date, time)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("选择: ${selectedDateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
                }
            }
        )

        // 时间轴视图
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 简单的日期筛选逻辑（示例）
            val filteredEvents = events.filter { event ->
                val eventDate = event.startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                eventDate == selectedDate
            }

            items(filteredEvents.sortedBy { it.startTime }) { event ->
                ScheduleEventItem(event = event)
            }

            // 添加空状态
            if (filteredEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "无日程",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "该日期没有安排日程",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ScheduleEventItem(event: ScheduleEvent) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间指示器
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        color = event.type.color,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "时间",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${timeFormatter.format(event.startTime)} - ${timeFormatter.format(event.endTime)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
