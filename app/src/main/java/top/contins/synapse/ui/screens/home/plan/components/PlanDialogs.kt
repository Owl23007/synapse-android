package top.contins.synapse.ui.screens.home.plan.components

import androidx.compose.runtime.Composable
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.model.RepeatRule
import top.contins.synapse.feature.task.ui.AddTaskDialog
import top.contins.synapse.feature.goal.ui.AddGoalDialog
import top.contins.synapse.feature.schedule.ui.AddScheduleDialog
import top.contins.synapse.domain.model.ScheduleType
import java.util.TimeZone
import java.util.UUID

/**
 * 计划页面对话框组件
 * 
 * 统一管理计划页面中的所有对话框：
 * - 添加任务对话框
 * - 添加目标对话框
 * - 添加日程对话框
 */
@Composable
fun PlanDialogs(
    showAddTaskDialog: Boolean,
    onDismissAddTask: () -> Unit,
    onConfirmAddTask: (title: String, priority: String, dueDate: String?) -> Unit,
    
    showAddGoalDialog: Boolean,
    onDismissAddGoal: () -> Unit,
    onConfirmAddGoal: (title: String, deadline: String) -> Unit,
    
    showAddScheduleDialog: Boolean,
    onDismissAddSchedule: () -> Unit,
    onConfirmAddSchedule: (
        title: String,
        startTime: Long,
        endTime: Long,
        location: String,
        isAllDay: Boolean,
        reminderMinutes: List<Int>?,
        repeatRule: RepeatRule?
    ) -> Unit
) {
    // 添加任务对话框
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = onDismissAddTask,
            onConfirm = { title, priority, dueDate ->
                onConfirmAddTask(title, priority, dueDate)
            }
        )
    }

    // 添加目标对话框
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = onDismissAddGoal,
            onConfirm = { title, deadline ->
                onConfirmAddGoal(title, deadline)
            }
        )
    }

    // 添加日程对话框
    if (showAddScheduleDialog) {
        AddScheduleDialog(
            onDismiss = onDismissAddSchedule,
            onConfirm = { title, startTime, endTime, location, isAllDay, reminderMinutes, repeatRule ->
                onConfirmAddSchedule(
                    title, startTime, endTime, location, isAllDay, reminderMinutes, repeatRule
                )
            }
        )
    }
}
