package top.contins.synapse.domain.repository

import top.contins.synapse.domain.model.schedule.Schedule

interface ReminderManager {
    suspend fun scheduleReminder(schedule: Schedule)
    suspend fun cancelReminder(schedule: Schedule)
}
