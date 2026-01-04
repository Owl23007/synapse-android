package top.contins.synapse.ui.screens.tabs

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import top.contins.synapse.domain.model.Goal
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.model.Task
import top.contins.synapse.domain.model.TaskPriority
import top.contins.synapse.domain.model.TaskStatus
import top.contins.synapse.feature.goal.viewmodel.GoalViewModel
import top.contins.synapse.feature.schedule.ui.ScheduleScreen
import top.contins.synapse.feature.schedule.viewmodel.ScheduleViewModel
import top.contins.synapse.feature.task.viewmodel.TaskViewModel
import top.contins.synapse.utils.BingImageHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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
    
    // 逾期任务：截止日期早于今天的未完成任务
    val overdueTasks = tasks.filter {
        val taskDate = it.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        taskDate.isBefore(today) && it.status != TaskStatus.COMPLETED
    }.sortedBy { it.dueDate }
    
    // 未来任务：截止日期晚于今天的未完成任务
    val upcomingTasks = tasks.filter {
        val taskDate = it.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        taskDate.isAfter(today) && it.status != TaskStatus.COMPLETED
    }.sortedBy { it.dueDate }
    
    val todaySchedules = schedules.filter {
        val scheduleDate = java.time.Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        scheduleDate.isEqual(today)
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
                    scheduleAddTick += 1 
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
                    overdueTasks = overdueTasks,
                    upcomingTasks = upcomingTasks,
                    schedules = todaySchedules,
                    onTaskStatusChange = { task, isCompleted ->
                        taskViewModel.updateTaskStatus(task, isCompleted)
                    },
                    onTaskDelete = { task ->
                        taskViewModel.deleteTask(task.id)
                    }
                )
                1 -> ScheduleScreen(viewModel = scheduleViewModel, showFab = false, addTick = scheduleAddTick)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, String?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("中") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableIntStateOf(20) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 6.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // 标题区
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "新建任务",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 分隔线
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 20.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // 任务内容区域
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "任务内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                // 任务内容输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("还有什么没做？快想想 ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 优先级选择区域
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "优先级",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "高" to Color(0xFFEF5350),
                        "中" to Color(0xFFFFA726),
                        "低" to Color(0xFF66BB6A)
                    ).forEach { (p, color) ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { 
                                Text(
                                    p, 
                                    fontWeight = FontWeight.Medium,
                                    color = if (priority == p) Color.White else color
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = color
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = priority == p,
                                borderColor = color.copy(alpha = 0.5f),
                                selectedBorderColor = color,
                                borderWidth = 1.5.dp
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 截止时间选择区域
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 标题行和清除按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "截止时间",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (selectedDateMillis != null) {
                        TextButton(
                            onClick = { selectedDateMillis = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // 快捷预设按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                        // 判断是否为今天内
                        val isTodayEnd = remember(selectedDateMillis, selectedHour, selectedMinute) {
                            selectedDateMillis?.let {
                                val selected = java.util.Calendar.getInstance()
                                selected.timeInMillis = it
                                val today = java.util.Calendar.getInstance()
                                selected.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                                selected.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) &&
                                selectedHour == 23 && selectedMinute == 59
                            } ?: false
                        }
                        
                        // 判断是否为明天
                        val isTomorrow = remember(selectedDateMillis, selectedHour, selectedMinute) {
                            selectedDateMillis?.let {
                                val selected = java.util.Calendar.getInstance()
                                selected.timeInMillis = it
                                val tomorrow = java.util.Calendar.getInstance()
                                tomorrow.add(java.util.Calendar.DAY_OF_MONTH, 1)
                                selected.get(java.util.Calendar.YEAR) == tomorrow.get(java.util.Calendar.YEAR) &&
                                selected.get(java.util.Calendar.DAY_OF_YEAR) == tomorrow.get(java.util.Calendar.DAY_OF_YEAR) &&
                                selectedHour == 23 && selectedMinute == 59
                            } ?: false
                        }
                        
                        // 判断是否为本周日
                        val isThisWeekEnd = remember(selectedDateMillis, selectedHour, selectedMinute) {
                            selectedDateMillis?.let {
                                val selected = java.util.Calendar.getInstance()
                                selected.timeInMillis = it
                                val thisWeekSunday = java.util.Calendar.getInstance()
                                val currentDayOfWeek = thisWeekSunday.get(java.util.Calendar.DAY_OF_WEEK)
                                val daysUntilSunday = (java.util.Calendar.SUNDAY - currentDayOfWeek + 7) % 7
                                if (daysUntilSunday == 0) {
                                    thisWeekSunday.add(java.util.Calendar.DAY_OF_MONTH, 7)
                                } else {
                                    thisWeekSunday.add(java.util.Calendar.DAY_OF_MONTH, daysUntilSunday)
                                }
                                selected.get(java.util.Calendar.YEAR) == thisWeekSunday.get(java.util.Calendar.YEAR) &&
                                selected.get(java.util.Calendar.DAY_OF_YEAR) == thisWeekSunday.get(java.util.Calendar.DAY_OF_YEAR) &&
                                selectedHour == 23 && selectedMinute == 59
                            } ?: false
                        }
                        
                        // 今天内
                        FilterChip(
                            selected = isTodayEnd,
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                calendar.set(java.util.Calendar.MINUTE, 59)
                                calendar.set(java.util.Calendar.SECOND, 59)
                                selectedDateMillis = calendar.timeInMillis
                                selectedHour = 23
                                selectedMinute = 59
                            },
                            label = { 
                                Text(
                                    "今天内", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    fontWeight = if (isTodayEnd) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isTodayEnd) Color.White else MaterialTheme.colorScheme.primary
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isTodayEnd,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp
                            )
                        )
                        
                        // 明天
                        FilterChip(
                            selected = isTomorrow,
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                calendar.set(java.util.Calendar.MINUTE, 59)
                                calendar.set(java.util.Calendar.SECOND, 59)
                                selectedDateMillis = calendar.timeInMillis
                                selectedHour = 23
                                selectedMinute = 59
                            },
                            label = { 
                                Text(
                                    "明天", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    fontWeight = if (isTomorrow) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isTomorrow) Color.White else MaterialTheme.colorScheme.primary
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isTomorrow,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp
                            )
                        )
                        
                        // 本周
                        FilterChip(
                            selected = isThisWeekEnd,
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                                val daysUntilSunday = (java.util.Calendar.SUNDAY - currentDayOfWeek + 7) % 7
                                if (daysUntilSunday == 0) {
                                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 7)
                                } else {
                                    calendar.add(java.util.Calendar.DAY_OF_MONTH, daysUntilSunday)
                                }
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                calendar.set(java.util.Calendar.MINUTE, 59)
                                calendar.set(java.util.Calendar.SECOND, 59)
                                selectedDateMillis = calendar.timeInMillis
                                selectedHour = 23
                                selectedMinute = 59
                            },
                            label = { 
                                Text(
                                    "本周", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    fontWeight = if (isThisWeekEnd) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isThisWeekEnd) Color.White else MaterialTheme.colorScheme.primary
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isThisWeekEnd,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp
                            )
                        )
                    }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedDateMillis != null) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (selectedDateMillis != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (selectedDateMillis != null) {
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.timeInMillis = selectedDateMillis!!
                                    dateFormatter.format(calendar.time)
                                } else {
                                    "点击设置截止时间"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedDateMillis != null) FontWeight.Medium else FontWeight.Normal,
                                color = if (selectedDateMillis != null)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (selectedDateMillis != null) {
                            Text(
                                text = String.format("%02d:%02d", selectedHour, selectedMinute),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 底部按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("取消", style = MaterialTheme.typography.bodyLarge)
                }
                
                Button(
                    onClick = { 
                        if (title.isNotBlank()) {
                            val finalDueDate = selectedDateMillis?.let { millis ->
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = millis
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                                calendar.set(java.util.Calendar.MINUTE, selectedMinute)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
                            }
                            onConfirm(title, priority, finalDueDate)
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("确定", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    
    // DatePicker Dialog
    if (showDatePicker) {
        val currentTime = System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: currentTime,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // 只允许选择今天及以后的日期
                    return utcTimeMillis >= currentTime - (24 * 60 * 60 * 1000)
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true // 选择完日期后显示时间选择器
                    }
                ) {
                    Text("下一步")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // TimePicker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 检查选择的时间是否早于当前时间
                        val calendar = java.util.Calendar.getInstance()
                        val now = java.util.Calendar.getInstance()
                        
                        calendar.timeInMillis = selectedDateMillis ?: System.currentTimeMillis()
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(java.util.Calendar.MINUTE, timePickerState.minute)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        
                        if (calendar.timeInMillis < now.timeInMillis) {
                            // 如果选择的时间早于当前时间，可以在这里显示提示
                            // 暂时直接使用当前时间
                            selectedHour = now.get(java.util.Calendar.HOUR_OF_DAY)
                            selectedMinute = now.get(java.util.Calendar.MINUTE)
                        } else {
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                        }
                        showTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun AddSimpleScheduleDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("09:00 - 10:00") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建日程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("日程标题") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("时间") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, time, location) },
                enabled = title.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("2024-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建目标") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("目标标题") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("截止日期") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, deadline) },
                enabled = title.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun TodayTab(
    todayTasks: List<Task>,
    overdueTasks: List<Task>,
    upcomingTasks: List<Task>,
    schedules: List<Schedule>,
    onTaskStatusChange: (Task, Boolean) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    val totalInboxTasks = overdueTasks.size + upcomingTasks.size
    val context = LocalContext.current
    val todayText = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())
    val dayOfWeekText = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
    
    // 必应图片URL状态
    var bingImageUrl by remember { mutableStateOf<String?>(null) }
    
    // 加载必应图片
    LaunchedEffect(Unit) {
        bingImageUrl = BingImageHelper.getTodayImageUrl(context)
    }
    
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val expandedHeight = 200.dp
    val collapsedHeight = 80.dp
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
    
    val maxCollapseDistance = expandedHeightPx - collapsedHeightPx
    
    // 计算目标折叠进度
    val targetCollapseFraction = remember { derivedStateOf {
        val firstVisibleItemIndex = listState.firstVisibleItemIndex
        val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
        
        if (firstVisibleItemIndex > 0) {
            1f
        } else {
            (firstVisibleItemScrollOffset / maxCollapseDistance).coerceIn(0f, 1f)
        }
    }}
    
    // 使用弹性动画
    val collapseFraction by animateFloatAsState(
        targetValue = targetCollapseFraction.value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "collapseFraction"
    )
    
    val headerOffset = -maxCollapseDistance * collapseFraction
    
    // 使用阈值切换，确保任何时候只显示一个状态
    val showExpanded = collapseFraction < 0.5f
    val showCollapsed = collapseFraction >= 0.5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(0.dp)) // 裁剪超出边界的内容
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spacer for Header - 固定高度
            item {
                Spacer(modifier = Modifier.height(expandedHeight))
            }

            // 今日概览
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TodayStatCard(
                        title = "今日任务",
                        count = "${todayTasks.size}",
                        subtitle = "待完成",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )
                    TodayStatCard(
                        title = "日程规划",
                        count = "${schedules.size}",
                        subtitle = "今日",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    TodayStatCard(
                        title = if (overdueTasks.isNotEmpty()) "逾期任务" else "待办事项",
                        count = if (overdueTasks.isNotEmpty()) "${overdueTasks.size}" else "$totalInboxTasks",
                        subtitle = if (overdueTasks.isNotEmpty()) "需处理" else "未来",
                        modifier = Modifier.weight(1f),
                        color = if (overdueTasks.isNotEmpty()) Color(0xFFEF5350) else MaterialTheme.colorScheme.secondary,
                        isHighlight = overdueTasks.isNotEmpty()
                    )
                }
            }
            
            // 今日任务列表
            if (todayTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "今日任务",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${todayTasks.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                items(todayTasks) { task ->
                    TodayTaskCard(
                        task = task,
                        onStatusChange = { onTaskStatusChange(task, it) },
                        onDelete = { onTaskDelete(task) }
                    )
                }
            }

            // 逾期任务列表
            if (overdueTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "逾期任务",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF5350)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF5350).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${overdueTasks.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF5350),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "需要处理",
                            fontSize = 12.sp,
                            color = Color(0xFFEF5350).copy(alpha = 0.7f)
                        )
                    }
                }
                
                items(overdueTasks) { task ->
                    OverdueTaskCard(
                        task = task,
                        onStatusChange = { onTaskStatusChange(task, it) },
                        onDelete = { onTaskDelete(task) }
                    )
                }
            }
            
            // 未来任务列表
            if (upcomingTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "未来任务",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${upcomingTasks.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "未来截止",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                items(upcomingTasks) { task ->
                    InboxTaskCard(
                        task = task,
                        onStatusChange = { onTaskStatusChange(task, it) },
                        onDelete = { onTaskDelete(task) }
                    )
                }
            }
            
            // 今日日程列表
            if (schedules.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "今日日程",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "${schedules.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                items(schedules) { schedule ->
                    TodayScheduleCard(schedule = schedule)
                }
            }
        }
        
        // Collapsing Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(expandedHeight)
                .graphicsLayer {
                    translationY = headerOffset
                }
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            // Expanded Content (Image + Text)
            if (showExpanded) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                // 必应每日一图背景
                if (bingImageUrl != null) {
                    AsyncImage(
                        model = bingImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.4f
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                }
                
                // 渐变遮罩，提高文字可读性
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = todayText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = dayOfWeekText,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            }
            
            // Collapsed Content
            if (showCollapsed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsedHeight)
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = todayText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = dayOfWeekText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TaskTab(
    tasks: List<Task>,
    onTaskStatusChange: (Task, Boolean) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            TaskCard(
                task = task,
                onStatusChange = { onTaskStatusChange(task, it) },
                onDelete = { onTaskDelete(task) }
            )
        }
    }
}

@Composable
fun GoalTab(goals: List<Goal>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals) { goal ->
            GoalCard(goal = goal)
        }
    }
}

// UI组件
@Composable
fun TodayStatCard(
    title: String,
    count: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isHighlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) 
                color.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isHighlight) color else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isHighlight) color.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart },
        positionalThreshold = { it * 0.5f }
    )

    // 监听dismiss状态，当完全滑动到位时执行删除
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            kotlinx.coroutines.delay(20) // 等待动画完成
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color.Red
            } else {
                Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.status == TaskStatus.COMPLETED,
                    onCheckedChange = onStatusChange
                )
                
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (task.priority) {
                                TaskPriority.HIGH, TaskPriority.URGENT -> Color.Red
                                TaskPriority.MEDIUM -> Color.Yellow
                                else -> Color.Gray
                            }
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Medium,
                        color = if (task.status == TaskStatus.COMPLETED) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart },
        positionalThreshold = { it * 0.5f }
    )

    // 监听dismiss状态，当完全滑动到位时执行删除
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            kotlinx.coroutines.delay(200) // 等待动画完成
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color.Red
            } else {
                Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = task.status == TaskStatus.COMPLETED,
                        onCheckedChange = onStatusChange,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null
                    )
                    Row {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (task.priority) {
                                TaskPriority.HIGH, TaskPriority.URGENT -> Color.Red.copy(alpha = 0.1f)
                                TaskPriority.MEDIUM -> Color.Yellow.copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                text = task.priority.displayName,
                                fontSize = 10.sp,
                                color = when (task.priority) {
                                    TaskPriority.HIGH, TaskPriority.URGENT -> Color.Red
                                    TaskPriority.MEDIUM -> Color.Yellow
                                    else -> Color.Gray
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 36.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "截止：${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.dueDate)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 36.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(goal: Goal) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = goal.title,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = goal.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "进度",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goal.progress}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { goal.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "目标时间：${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(goal.targetDate)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueTaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart },
        positionalThreshold = { it * 0.5f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            kotlinx.coroutines.delay(20)
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color.Red
            } else {
                Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF5F5)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.status == TaskStatus.COMPLETED,
                    onCheckedChange = onStatusChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFEF5350),
                        uncheckedColor = Color(0xFFEF5350)
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF5350))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 计算逾期天数
                    val dueDate = task.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val today = LocalDate.now()
                    val daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "已逾期 ${daysOverdue} 天",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF5350)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxTaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart },
        positionalThreshold = { it * 0.5f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            kotlinx.coroutines.delay(20)
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color.Red
            } else {
                Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.status == TaskStatus.COMPLETED,
                    onCheckedChange = onStatusChange
                )
                
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (task.priority) {
                                TaskPriority.HIGH, TaskPriority.URGENT -> Color.Red
                                TaskPriority.MEDIUM -> Color.Yellow
                                else -> Color.Gray
                            }
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 显示截止日期
                    val dueDate = task.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val today = LocalDate.now()
                    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
                    
                    val dueDateText = when {
                        daysUntil == 1L -> "明天截止"
                        daysUntil <= 7 -> "${daysUntil} 天后截止"
                        else -> SimpleDateFormat("MM月dd日", Locale.getDefault()).format(task.dueDate)
                    }
                    
                    val dueDateColor = when {
                        daysUntil <= 1 -> Color(0xFFFFA726)
                        daysUntil <= 3 -> Color(0xFFFFA726).copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    Text(
                        text = dueDateText,
                        fontSize = 12.sp,
                        color = dueDateColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScheduleCard(schedule: Schedule) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(schedule.startTime))} - ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(schedule.endTime))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!schedule.location.isNullOrEmpty()) {
                    Text(
                        text = schedule.location!!,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}