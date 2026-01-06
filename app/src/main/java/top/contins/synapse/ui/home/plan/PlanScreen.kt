package top.contins.synapse.ui.home.plan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.ScheduleType
import top.contins.synapse.domain.model.task.Task
import top.contins.synapse.domain.model.task.TaskStatus
import top.contins.synapse.feature.goal.viewmodel.GoalViewModel
import top.contins.synapse.feature.schedule.ui.ScheduleTab
import top.contins.synapse.feature.schedule.viewmodel.ScheduleViewModel
import top.contins.synapse.feature.task.viewmodel.TaskViewModel
import top.contins.synapse.feature.today.ui.TodayTab
import top.contins.synapse.feature.task.ui.TaskTab
import top.contins.synapse.feature.goal.ui.GoalTab
import top.contins.synapse.network.utils.BingImageHelper
import top.contins.synapse.ui.home.plan.components.ExpandableFab
import top.contins.synapse.ui.home.plan.components.PlanDialogs
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone
import java.util.UUID

/**
 * 计划页面容器
 * 
 * 协调多个功能 Tab 的展示和交互：
 * - 今日 Tab (feature:today)
 * - 日程 Tab (feature:schedule)
 * - 任务 Tab (feature:task)
 * - 目标 Tab (feature:goal)
 * 
 * 职责：
 * - 管理 Tab 切换状态
 * - 提供扩展 FAB 和相关 dialog
 * - 协调各 feature 的数据流
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
    var editingTask by remember { mutableStateOf<Task?>(null) }

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
        val scheduleDate = Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
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
                    editingTask = null
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
                        onClick = { 
                            selectedTab = index
                            isFabExpanded = false
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Tab 内容
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
                    },
                    onTaskEdit = { task ->
                        editingTask = task
                        showAddTaskDialog = true
                    }
                )
                3 -> GoalTab(goals)
            }
        }
    }

    // 对话框层
    PlanDialogs(
        showAddTaskDialog = showAddTaskDialog,
        editingTask = editingTask,
        onDismissAddTask = {
            showAddTaskDialog = false
            editingTask = null
        },
        onConfirmAddTask = { title, priority, dueDate ->
            val task = editingTask
            if (task == null) {
                taskViewModel.createTask(title, priority, dueDate)
            } else {
                taskViewModel.updateTask(task, title, priority, dueDate)
            }
            showAddTaskDialog = false
            editingTask = null
        },

        showAddGoalDialog = showAddGoalDialog,
        onDismissAddGoal = { showAddGoalDialog = false },
        onConfirmAddGoal = { title, deadline ->
            goalViewModel.createGoal(title, deadline)
            showAddGoalDialog = false
        },

        showAddScheduleDialog = showAddScheduleDialog,
        onDismissAddSchedule = { showAddScheduleDialog = false },
        onConfirmAddSchedule = { title, startTime, endTime, location, isAllDay, reminderMinutes, repeatRule ->
            val currentTime = System.currentTimeMillis()
            val schedule = Schedule(
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
                repeatRule = repeatRule,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            scheduleViewModel.createSchedule(schedule)
            showAddScheduleDialog = false
        }
    )
}
