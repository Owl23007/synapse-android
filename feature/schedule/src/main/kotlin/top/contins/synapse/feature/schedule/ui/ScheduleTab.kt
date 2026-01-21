package top.contins.synapse.feature.schedule.ui

import android.widget.Toast
import java.time.YearMonth
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.ScheduleType
import top.contins.synapse.feature.schedule.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import java.util.UUID

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.input.pointer.pointerInput

import java.time.temporal.WeekFields
import java.util.Locale
import top.contins.synapse.feature.schedule.ui.WeekTimeSlotsView
import top.contins.synapse.feature.schedule.ui.CalendarViewMode

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

/**
 * 日程 Tab - 用于在 PlanScreen 中显示日程内容
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ScheduleTab(
    viewModel: ScheduleViewModel = hiltViewModel(),
    addTick: Int = 0,
    onShowAddDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }

    // 权限请求 Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "请开启通知权限以接收日程提醒", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        // 1. 请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // 2. 检查电池优化 (简单引导)
        if (!viewModel.isBatteryOptimizationIgnored()) {
            showBatteryOptimizationDialog = true
        }
    }

    if (showBatteryOptimizationDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryOptimizationDialog = false },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
            title = { Text("后台运行权限") },
            text = { Text("为了确保闹钟和提醒准时触发，Synapse 需要在后台运行。请允许应用忽略电池优化。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.requestIgnoreBatteryOptimizations { intent ->
                        context.startActivity(intent)
                    }
                    showBatteryOptimizationDialog = false
                }) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryOptimizationDialog = false }) {
                    Text("稍后")
                }
            }
        )
    }

    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val schedulesMap by viewModel.schedulesMap.collectAsState()
    val selectedDateSchedules by viewModel.selectedDateSchedules.collectAsState()
    val weekSchedules by viewModel.weekSchedules.collectAsState()
    
    var viewMode by remember { mutableStateOf(CalendarViewMode.Month) }
    var isDayDetailVisible by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
    var initialAddDate by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(addTick) {
        if (addTick > 0) {
            editingSchedule = null
            initialAddDate = null
            showAddDialog = true
        }
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = isDayDetailVisible,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "DayDetailTransition"
        ) { isDetail ->
            if (isDetail) {
                DayScheduleView(
                    animatedVisibilityScope = this@AnimatedContent,
                    date = selectedDate,
                    schedules = selectedDateSchedules,
                    onBack = { isDayDetailVisible = false },
                    onAddSchedule = { timestamp ->
                        initialAddDate = timestamp
                        showAddDialog = true
                    },
                    onEditSchedule = { schedule ->
                        editingSchedule = schedule
                        showAddDialog = true
                    }
                )
            } else {
                val mainScope = this@AnimatedContent
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
        // Calendar Area with Drag Gesture
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var accumulatedDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onDragEnd = { accumulatedDrag = 0f }
                    ) { _, dragAmount ->
                        accumulatedDrag += dragAmount
                        
                        // Drag Up: Switch to Week View (False)
                        if (accumulatedDrag < -50 && viewMode == CalendarViewMode.Month) { 
                            viewMode = CalendarViewMode.Week
                            accumulatedDrag = 0f
                        } 
                        // Drag Down: Switch to Month View (True)
                        else if (accumulatedDrag > 50 && viewMode == CalendarViewMode.Week) { 
                            viewMode = CalendarViewMode.Month
                            accumulatedDrag = 0f
                        }
                    }
                },
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
            ) {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onMonthClick = { showDatePicker = true },
                    onTodayClick = {
                        viewModel.onDateSelected(java.time.LocalDate.now())
                        viewModel.onMonthChanged(java.time.YearMonth.now())
                    },
                    onAddClick = {
                        initialAddDate = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        showAddDialog = true
                    }
                )

                DaysOfWeekTitle(daysOfWeek = remember { daysOfWeek() })
                
                // Switch between Month and Week View
                AnimatedContent(
                    targetState = viewMode == CalendarViewMode.Month,
                    transitionSpec = {
                        if (targetState) {
                             expandVertically(expandFrom = Alignment.Top) + fadeIn() togetherWith fadeOut(animationSpec = tween(100))
                        } else {
                             fadeIn(animationSpec = tween(100)) togetherWith shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                        }
                    },
                    label = "CalendarViewMode"
                ) { isMonth ->
                   if (isMonth) {
                       MonthView(
                           sharedTransitionScope = this@SharedTransitionLayout,
                           animatedVisibilityScope = mainScope,
                           modifier = Modifier.fillMaxWidth(),
                           currentMonth = currentMonth,
                           selectedDate = selectedDate,
                           schedulesMap = schedulesMap,
                           onDateSelected = { date ->
                               val prev = selectedDate
                               viewModel.onDateSelected(date)
                               if (date == prev) isDayDetailVisible = true
                           },
                           onMonthChanged = viewModel::onMonthChanged
                       )
                   } else {
                       WeekCalendarPager(
                           sharedTransitionScope = this@SharedTransitionLayout,
                           animatedVisibilityScope = mainScope,
                           modifier = Modifier.fillMaxWidth(),
                           selectedDate = selectedDate,
                           schedulesMap = schedulesMap,
                           onDateSelected = { date ->
                               val prev = selectedDate
                               viewModel.onDateSelected(date)
                               viewModel.onMonthChanged(YearMonth.from(date))
                               if (date == prev) isDayDetailVisible = true
                           }
                       )
                   }
                }

                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        // Content Switch: Month(List) / Week(Grid)
        AnimatedContent(
            targetState = viewMode == CalendarViewMode.Month,
            transitionSpec = {
                 fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "ContentTransition",
            modifier = Modifier.weight(1f)
        ) { isMonth ->
            if (isMonth) {
                // Month View -> Show Daily Schedule List
                ScheduleList(
                    modifier = Modifier.fillMaxSize(),
                    schedules = selectedDateSchedules,
                    onDelete = viewModel::deleteSchedule,
                    onEdit = { schedule ->
                        editingSchedule = schedule
                        showAddDialog = true
                    }
                )
            } else {
                 // Week View -> Show Week Time Grid (Timeline)
                 // Need to calculate current week dates relative to selectedDate
                 val weekStart = remember(selectedDate) {
                      val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                      val diff = (selectedDate.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
                      selectedDate.minusDays(diff.toLong())
                 }
                 val weekDates = remember(weekStart) {
                     (0..6).map { weekStart.plusDays(it.toLong()) }
                 }

                 WeekTimeSlotsView(
                     weekDates = weekDates,
                     schedules = weekSchedules,
                     onTimeSlotClick = { date, time -> 
                        initialAddDate = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        showAddDialog = true
                     },
                     onScheduleClick = { schedule ->
                        editingSchedule = schedule
                        showAddDialog = true
                     }
                 )
            }
        }
    }
                }
            }
        }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentMonth.atDay(1)
                .atStartOfDay(java.time.ZoneId.of("UTC")) // Use UTC to avoid timezone shift in Picker
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onMonthChanged(java.time.YearMonth.from(date))
                            viewModel.onDateSelected(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
    if (showAddDialog) {
        val deleteHandler: (() -> Unit)? = if (editingSchedule != null) {
            {
                viewModel.deleteSchedule(editingSchedule!!)
                showAddDialog = false
                editingSchedule = null
                initialAddDate = null
            }
        } else null

        AddScheduleDialog(
            initialDate = initialAddDate ?: editingSchedule?.startTime
                ?: selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            initialSchedule = editingSchedule,
            onDismiss = {
                showAddDialog = false
                editingSchedule = null
                initialAddDate = null
            },
            onDelete = deleteHandler,
            onConfirm = { title, startTime, endTime, location, isAllDay, reminderMinutes, isAlarm, repeatRule ->
                scope.launch {
                    val conflicts = viewModel.checkConflict(startTime, endTime)
                    val count = conflicts.filter { it.id != editingSchedule?.id }.size
                    
                    if (count > 0) {
                        Toast.makeText(context, "注意：该时段已有 $count 个日程冲突", Toast.LENGTH_SHORT).show()
                    }

                    val currentTime = System.currentTimeMillis()
                    val schedule = editingSchedule?.copy(
                        title = title,
                        startTime = startTime,
                        endTime = endTime,
                        location = location.ifBlank { null },
                        isAllDay = isAllDay,
                        reminderMinutes = reminderMinutes,
                        isAlarm = isAlarm,
                        repeatRule = repeatRule,
                        updatedAt = currentTime
                    ) ?: Schedule(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        startTime = startTime,
                        endTime = endTime,
                        timezoneId = TimeZone.getDefault().id,
                        location = location.ifBlank { null },
                        type = ScheduleType.EVENT,
                        calendarId = "default",
                        isAllDay = isAllDay,
                        reminderMinutes = reminderMinutes,
                        isAlarm = isAlarm,
                        repeatRule = repeatRule,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    )

                    if (editingSchedule == null) {
                        viewModel.createSchedule(schedule)
                    } else {
                        viewModel.updateSchedule(schedule)
                    }

                    showAddDialog = false
                    editingSchedule = null
                    initialAddDate = null
                }
            }
        )
    }
}

@Composable
fun ScheduleList(
    modifier: Modifier = Modifier,
    schedules: List<Schedule>,
    onDelete: (Schedule) -> Unit,
    onEdit: (Schedule) -> Unit
) {
    if (schedules.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "今日暂无安排",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = schedules, key = { it.id }) { schedule ->
                ScheduleItem(
                    schedule = schedule,
                    onDelete = onDelete,
                    onEdit = onEdit
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItem(schedule: Schedule, onDelete: (Schedule) -> Unit, onEdit: (Schedule) -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(schedule)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                }, label = "background"
            )
            
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = {
            ScheduleCardContent(schedule = schedule, onEdit = onEdit)
        }
    )
}

@Composable
fun ScheduleCardContent(schedule: Schedule, onEdit: (Schedule) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(schedule) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            // Color Strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(Color(schedule.color ?: 0xFF2196F3))
            )
            
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Column
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (schedule.isAllDay) {
                        Text(
                            text = "全天",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = formatTime(schedule.startTime),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = calculateDuration(schedule.startTime, schedule.endTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                // Divider line (Visual)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                
                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val location = schedule.location
                    val description = schedule.description

                    if (!location.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (!description.isNullOrBlank()) {
                         Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
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

fun calculateDuration(start: Long, end: Long): String {
    val diff = end - start
    if (diff <= 0) return ""
    val hours = diff / (1000 * 60 * 60)
    val minutes = (diff / (1000 * 60)) % 60
    return if (hours > 0) "${hours}h${if (minutes > 0) " ${minutes}m" else ""}" else "${minutes}m"
}
