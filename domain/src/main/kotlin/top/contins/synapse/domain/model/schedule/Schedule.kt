package top.contins.synapse.domain.model.schedule

data class Schedule(
    val id: String,
    val title: String,
    val description: String? = null,
    val startTime: Long, // UTC timestamp
    val endTime: Long,   // UTC timestamp
    val timezoneId: String,
    val location: String? = null,
    val type: ScheduleType,
    val color: Long? = null,
    val reminderMinutes: List<Int>? = null,
    val repeatRule: RepeatRule? = null,
    val calendarId: String,
    val isAllDay: Boolean = false,
    val isFromSubscription: Boolean = false,
    val subscriptionId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
