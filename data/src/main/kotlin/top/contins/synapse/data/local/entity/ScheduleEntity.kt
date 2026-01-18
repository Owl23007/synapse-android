package top.contins.synapse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = CalendarEntity::class,
            parentColumns = ["id"],
            childColumns = ["calendar_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["start_time_utc", "end_time_utc"], name = "idx_schedules_time"),
        Index(value = ["calendar_id", "start_time_utc"], name = "idx_schedules_calendar_time"),
        Index(value = ["subscription_id"], name = "idx_schedules_subscription")
    ]
)
data class ScheduleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "start_time_utc")
    val startTimeUtc: Long,
    @ColumnInfo(name = "end_time_utc")
    val endTimeUtc: Long,
    @ColumnInfo(name = "timezone_id")
    val timezoneId: String,
    val location: String?,
    val type: String, // Enum stored as String
    val color: Long?,
    @ColumnInfo(name = "reminder_minutes")
    val reminderMinutes: String?, // JSON List<Int>
    @ColumnInfo(name = "is_alarm", defaultValue = "0")
    val isAlarm: Boolean = false,
    @ColumnInfo(name = "repeat_rule")
    val repeatRule: String?, // JSON Object
    @ColumnInfo(name = "calendar_id")
    val calendarId: String,
    @ColumnInfo(name = "is_all_day", defaultValue = "0")
    val isAllDay: Boolean = false,
    @ColumnInfo(name = "is_from_subscription", defaultValue = "0")
    val isFromSubscription: Boolean = false,
    @ColumnInfo(name = "subscription_id")
    val subscriptionId: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
