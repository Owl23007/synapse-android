package top.contins.synapse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendars")
data class CalendarEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: Long,
    @ColumnInfo(name = "is_visible", defaultValue = "1")
    val isVisible: Boolean = true,
    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Boolean = false,
    @ColumnInfo(name = "default_reminder_minutes")
    val defaultReminderMinutes: String? = null, // JSON List<Int>
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
