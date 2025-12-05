package top.contins.synapse.domain.model

import java.util.Date

data class Schedule(
    val id: String,
    val title: String,
    val description: String,
    val startTime: Date,
    val endTime: Date,
    val location: String,
    val type: ScheduleType
)
