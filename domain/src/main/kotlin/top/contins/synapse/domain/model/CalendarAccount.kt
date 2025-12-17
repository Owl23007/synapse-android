package top.contins.synapse.domain.model

data class CalendarAccount(
    val id: String,
    val name: String,
    val color: Long,
    val isVisible: Boolean = true,
    val isDefault: Boolean = false,
    val defaultReminderMinutes: List<Int>? = null,
    val createdAt: Long,
    val updatedAt: Long
)
