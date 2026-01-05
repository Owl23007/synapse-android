package top.contins.synapse.ui.screens.tabs

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.model.TaskStatus
import top.contins.synapse.feature.goal.viewmodel.GoalViewModel
import top.contins.synapse.feature.schedule.ui.ScheduleTab
import top.contins.synapse.feature.schedule.viewmodel.ScheduleViewModel
import top.contins.synapse.feature.task.viewmodel.TaskViewModel
import top.contins.synapse.feature.today.ui.TodayTab
import top.contins.synapse.feature.task.ui.TaskTab
import top.contins.synapse.feature.goal.ui.GoalTab
import top.contins.synapse.feature.task.ui.AddTaskDialog
import top.contins.synapse.feature.goal.ui.AddGoalDialog
import top.contins.synapse.feature.schedule.ui.AddScheduleDialog
import top.contins.synapse.network.utils.BingImageHelper
import java.time.LocalDate
import java.time.ZoneId

/**
 * 计划页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun PlanScreen(
    taskViewModel: TaskViewModel = hiltViewModel(),
    goalViewModel: GoalViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("今日", "日程", "任务", "目标")
    
    // FAB展开状态
    var isFabExpanded by remember { mutableStateOf(false) }
    
    // 模态框状态
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var scheduleAddTick by remember { mutableIntStateOf(0) }

    // 数据状态
    val tasks by taskViewModel.tasks.collectAsState()
    val goals by goalViewModel.goals.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState()

    // 筛选今日任务和日程
    val today = LocalDate.now()
    
    // 今日任务：今天截止的未完成任务
    val todayTasks = tasks.filter { 
        val taskDate = it.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        taskDate.isEqual(today) && it.status != TaskStatus.COMPLETED
    }
    
    val todaySchedules = schedules.filter {
        val scheduleDate = java.time.Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        scheduleDate.isEqual(today)
    }

    val context = LocalContext.current
    var bingImageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        bingImageUrl = BingImageHelper.getTodayImageUrl(context)
    }

    Scaffold(
        floatingActionButton = {
            ExpandableFab(
                expanded = isFabExpanded,
                onExpandToggle = { isFabExpanded = !isFabExpanded },
                selectedTab = selectedTab,
                onTaskAdd = { 
                    isFabExpanded = false
                    showAddTaskDialog = true 
                },
                onScheduleAdd = { 
                    isFabExpanded = false
                    if (selectedTab == 0) {
                        showAddScheduleDialog = true
                    } else {
                        scheduleAddTick += 1
                    }
                },
                onGoalAdd = { 
                    isFabExpanded = false
                    showAddGoalDialog = true 
                }
            )
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Tab栏
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TodayTab(
                    todayTasks = todayTasks,
                    schedules = todaySchedules,
                    bingImageUrl = bingImageUrl,
                    onTaskStatusChange = { task, isCompleted ->
                        taskViewModel.updateTaskStatus(task, isCompleted)
                    },
                    onTaskDelete = { task ->
                        taskViewModel.deleteTask(task.id)
                    }
                )
                1 -> ScheduleTab(viewModel = scheduleViewModel, addTick = scheduleAddTick)
                2 -> TaskTab(
                    tasks = tasks,
                    onTaskStatusChange = { task, isCompleted ->
                        taskViewModel.updateTaskStatus(task, isCompleted)
                    },
                    onTaskDelete = { task ->
                        taskViewModel.deleteTask(task.id)
                    }
                )
                3 -> GoalTab(goals)
            }
        }
    }

    // Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, priority, dueDate ->
                taskViewModel.createTask(title, priority, dueDate)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, deadline ->
                goalViewModel.createGoal(title, deadline)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddScheduleDialog) {
        AddScheduleDialog(
            onDismiss = { showAddScheduleDialog = false },
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
                scheduleViewModel.createSchedule(schedule)
                showAddScheduleDialog = false
            }
        )
    }
}

@Composable
fun ExpandableFab(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    selectedTab: Int,
    onTaskAdd: () -> Unit,
    onScheduleAdd: () -> Unit,
    onGoalAdd: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "rotation"
    )
    
    val fabColor by animateColorAsState(
        targetValue = if (expanded) Color.White else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "fabColor"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )
    
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 小FAB按钮（根据当前tab显示不同选项）
        if (selectedTab == 0) {
            // 今日tab：显示任务和日程按钮
            MiniFab(
                icon = Icons.Filled.Event,
                text = "日程",
                onClick = onScheduleAdd,
                expanded = expanded
            )
            MiniFab(
                icon = Icons.Filled.Task,
                text = "任务",
                onClick = onTaskAdd,
                expanded = expanded
            )
        } else {
            // 其他tab：显示单个对应按钮
            val (icon, text, onClick) = when (selectedTab) {
                1 -> Triple(Icons.Filled.Event, "日程", onScheduleAdd)
                2 -> Triple(Icons.Filled.Task, "任务", onTaskAdd)
                3 -> Triple(Icons.Filled.Flag, "目标", onGoalAdd)
                else -> Triple(Icons.Filled.Add, "", {})
            }
            
            MiniFab(
                icon = icon,
                text = text,
                onClick = onClick,
                expanded = expanded
            )
        }
        
        // 主FAB
        FloatingActionButton(
            onClick = onExpandToggle,
            containerColor = fabColor,
            contentColor = iconColor
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加",
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            )
        }
    }
}

@Composable
fun MiniFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    expanded: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    if (expanded) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp)
                )
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

