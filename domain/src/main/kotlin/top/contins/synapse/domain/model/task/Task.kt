package top.contins.synapse.domain.model.task

import java.util.Date

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: Date?,
    val status: TaskStatus,
    val priority: TaskPriority
)
