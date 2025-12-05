package top.contins.synapse.domain.model

import java.util.Date

data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Date,
    val targetDate: Date,
    val isCompleted: Boolean = false,
    val progress: Int = 0 // 0-100
)
