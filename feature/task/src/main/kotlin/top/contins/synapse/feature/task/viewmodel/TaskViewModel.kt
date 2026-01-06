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
            val parsedDueDate = if (!dueDate.isNullOrBlank()) {
                val formats = listOf(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy-MM-dd"
                )
                var date: Date? = null
                for (format in formats) {
                    try {
                        date = SimpleDateFormat(format, Locale.getDefault()).parse(dueDate)
                        if (date != null) break
                    } catch (e: Exception) {
                        continue
                    }
                }
                date ?: Date()
            } else {
                Date() // 未提供截止日期时默认使用今天
            }
            
            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = "",
                dueDate = parsedDueDate,
                status = TaskStatus.TODO,
                priority = when(priority) {
                    "高" -> TaskPriority.HIGH
                    "中" -> TaskPriority.MEDIUM
                    "低" -> TaskPriority.LOW
                    else -> TaskPriority.MEDIUM
                }
            )
            taskRepository.createTask(newTask)
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
}
