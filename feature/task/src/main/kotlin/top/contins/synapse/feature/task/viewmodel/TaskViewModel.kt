package top.contins.synapse.feature.task.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.task.Task
import top.contins.synapse.domain.model.task.TaskPriority
import top.contins.synapse.domain.model.task.TaskStatus
import top.contins.synapse.domain.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * 任务功能的ViewModel
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTask(title: String, priority: String, dueDate: String? = null) {
        viewModelScope.launch {
            val parsedDueDate = parseDueDateOrDefault(dueDate)

            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = "",
                dueDate = parsedDueDate,
                status = TaskStatus.TODO,
                priority = toPriority(priority)
            )
            taskRepository.createTask(newTask)
        }
    }

    fun updateTask(task: Task, title: String, priority: String, dueDate: String?) {
        viewModelScope.launch {
            val parsedDueDate = parseDueDateOrDefault(dueDate)
            val updatedTask = task.copy(
                title = title,
                dueDate = parsedDueDate,
                priority = toPriority(priority)
            )
            taskRepository.updateTask(updatedTask)
        }
    }

    fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
            )
            taskRepository.updateTask(updatedTask)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    private fun parseDueDateOrDefault(dueDate: String?): Date? {
        if (dueDate.isNullOrBlank()) return null

        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd"
        )
        for (format in formats) {
            try {
                val parsed = SimpleDateFormat(format, Locale.getDefault()).parse(dueDate)
                if (parsed != null) return parsed
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    private fun toPriority(priority: String): TaskPriority = when (priority) {
        "高" -> TaskPriority.HIGH
        "中" -> TaskPriority.MEDIUM
        "低" -> TaskPriority.LOW
        "紧急" -> TaskPriority.URGENT
        else -> TaskPriority.MEDIUM
    }
}
