package top.contins.synapse.data.local.converter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import top.contins.synapse.data.local.entity.CalendarEntity
import top.contins.synapse.data.local.entity.ScheduleEntity
import top.contins.synapse.data.local.entity.SubscriptionEntity
import top.contins.synapse.domain.model.schedule.CalendarAccount
import top.contins.synapse.domain.model.schedule.RepeatRule
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.ScheduleType
import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.data.local.entity.GoalEntity
import top.contins.synapse.data.local.entity.TaskEntity
import top.contins.synapse.domain.model.goal.Goal
import top.contins.synapse.domain.model.task.Task
import top.contins.synapse.domain.model.task.TaskPriority
import top.contins.synapse.domain.model.task.TaskStatus
import java.util.Date

object DataMapper {
    private val gson = Gson()

    fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate?.let { Date(it) },
            status = TaskStatus.valueOf(status),
            priority = TaskPriority.valueOf(priority),
            createdAt = Date(createdAt),
            completedAt = completedAt?.let { Date(it) }
        )
    }

    fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate?.time,
            status = status.name,
            priority = priority.name,
            createdAt = createdAt.time,
            updatedAt = System.currentTimeMillis(),
            completedAt = completedAt?.time
        )
    }

    fun GoalEntity.toDomain(): Goal {
        return Goal(
            id = id,
            title = title,
            description = description,
            startDate = Date(startDate),
            targetDate = Date(targetDate),
            isCompleted = isCompleted,
            progress = progress
        )
    }

    fun Goal.toEntity(): GoalEntity {
        return GoalEntity(
            id = id,
            title = title,
            description = description,
            startDate = startDate.time,
            targetDate = targetDate.time,
            isCompleted = isCompleted,
            progress = progress
        )
    }

    fun CalendarEntity.toDomain(): CalendarAccount {
        val reminderList = defaultReminderMinutes?.let {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson<List<Int>>(it, type)
        }
        return CalendarAccount(
            id = id,
            name = name,
            color = color,
            isVisible = isVisible,
            isDefault = isDefault,
            defaultReminderMinutes = reminderList,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun CalendarAccount.toEntity(): CalendarEntity {
        val reminderJson = defaultReminderMinutes?.let { gson.toJson(it) }
        return CalendarEntity(
            id = id,
            name = name,
            color = color,
            isVisible = isVisible,
            isDefault = isDefault,
            defaultReminderMinutes = reminderJson,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun SubscriptionEntity.toDomain(): Subscription {
        return Subscription(
            id = id,
            name = name,
            url = url,
            color = color,
            syncInterval = syncInterval,
            lastSyncAt = lastSyncAt,
            isEnabled = isEnabled,
            createdAt = createdAt
        )
    }

    fun Subscription.toEntity(): SubscriptionEntity {
        return SubscriptionEntity(
            id = id,
            name = name,
            url = url,
            color = color,
            syncInterval = syncInterval,
            lastSyncAt = lastSyncAt,
            isEnabled = isEnabled,
            createdAt = createdAt
        )
    }

    fun ScheduleEntity.toDomain(): Schedule {
        val reminderList = reminderMinutes?.let {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson<List<Int>>(it, type)
        }
        val repeatRuleObj = repeatRule?.let {
            gson.fromJson(it, RepeatRule::class.java)
        }
        return Schedule(
            id = id,
            title = title,
            description = description,
            startTime = startTimeUtc,
            endTime = endTimeUtc,
            timezoneId = timezoneId,
            location = location,
            type = ScheduleType.valueOf(type),
            color = color,
            reminderMinutes = reminderList,
            isAlarm = isAlarm,
            repeatRule = repeatRuleObj,
            calendarId = calendarId,
            isAllDay = isAllDay,
            isFromSubscription = isFromSubscription,
            subscriptionId = subscriptionId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun Schedule.toEntity(): ScheduleEntity {
        val reminderJson = reminderMinutes?.let { gson.toJson(it) }
        val repeatRuleJson = repeatRule?.let { gson.toJson(it) }
        return ScheduleEntity(
            id = id,
            title = title,
            description = description,
            startTimeUtc = startTime,
            endTimeUtc = endTime,
            timezoneId = timezoneId,
            location = location,
            type = type.name,
            color = color,
            reminderMinutes = reminderJson,
            isAlarm = isAlarm,
            repeatRule = repeatRuleJson,
            calendarId = calendarId,
            isAllDay = isAllDay,
            isFromSubscription = isFromSubscription,
            subscriptionId = subscriptionId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
