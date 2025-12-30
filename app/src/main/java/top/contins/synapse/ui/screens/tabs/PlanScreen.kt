package top.contins.synapse.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.domain.model.Goal
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.model.Task
import top.contins.synapse.domain.model.TaskPriority
import top.contins.synapse.domain.model.TaskStatus
import top.contins.synapse.feature.goal.viewmodel.GoalViewModel
import top.contins.synapse.feature.schedule.ui.ScheduleScreen
import top.contins.synapse.feature.schedule.viewmodel.ScheduleViewModel
import top.contins.synapse.feature.task.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

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
    
    // State for dialogs
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showTodayActionDialog by remember { mutableStateOf(false) }
    var scheduleAddTick by remember { mutableIntStateOf(0) }

    // Data from ViewModels
    val tasks by taskViewModel.tasks.collectAsState()
    val goals by goalViewModel.goals.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState()

    // Filter for Today
    val today = LocalDate.now()
    val todayTasks = tasks.filter { 
        val taskDate = it.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        taskDate.isEqual(today)
    }
    val todaySchedules = schedules.filter {
        val scheduleDate = java.time.Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        scheduleDate.isEqual(today)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    when (selectedTab) {
                        0 -> showTodayActionDialog = true
                        1 -> scheduleAddTick += 1
                        2 -> showAddTaskDialog = true
                        3 -> showAddGoalDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加")
            }
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
                0 -> TodayTab(todayTasks, todaySchedules)
                1 -> ScheduleScreen(viewModel = scheduleViewModel, showFab = false, addTick = scheduleAddTick)
                2 -> TaskTab(tasks)
                3 -> GoalTab(goals)
            }
        }
    }

    // Dialogs
    if (showTodayActionDialog) {
        AlertDialog(
            onDismissRequest = { showTodayActionDialog = false },
            title = { Text("添加今日事项") },
            text = {
                Column {
                    TextButton(
                        onClick = { 
                            showTodayActionDialog = false
                            showAddTaskDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("添加任务")
                    }
                    TextButton(
                        onClick = { 
                            showTodayActionDialog = false
                            scheduleAddTick += 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("添加日程")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTodayActionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, priority ->
                taskViewModel.createTask(title, priority)
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
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("中") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建任务") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题") },
                    singleLine = true
                )
                // Simple Priority Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("优先级: ")
                    listOf("高", "中", "低").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, priority) },
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
    tasks: List<Task>,
    schedules: List<Schedule>
) {
    val todayText = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())
    val dayOfWeekText = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 日期显示
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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
        
        // 今日概览
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodayStatCard("待办事项", "${tasks.size}", "${tasks.count { it.status == TaskStatus.COMPLETED }}已完成", modifier = Modifier.weight(1f))
                TodayStatCard("会议安排", "${schedules.size}", "即将开始", modifier = Modifier.weight(1f))
                TodayStatCard("目标进度", "0", "进行中", modifier = Modifier.weight(1f))
            }
        }
        
        // 今日任务列表
        item {
            Text(
                text = "今日任务",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        items(tasks) { task ->
            TodayTaskCard(task = task)
        }

        // 今日日程列表
        item {
            Text(
                text = "今日日程",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        items(schedules) { schedule ->
            TodayScheduleCard(schedule = schedule)
        }
    }
}

@Composable
fun TaskTab(tasks: List<Task>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            TaskCard(task = task)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTaskCard(task: Task) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.status == TaskStatus.COMPLETED) Color.Green
                        else when (task.priority) {
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
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (task.status != TaskStatus.COMPLETED) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { }
                )
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "已完成",
                    tint = Color.Green
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(task: Task) {
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
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
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
                    if (task.status == TaskStatus.COMPLETED) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "已完成",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = task.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "截止：${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.dueDate)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                    progress = goal.progress / 100f,
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
fun TodayScheduleCard(schedule: Schedule) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
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