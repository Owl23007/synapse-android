package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ReminderManager
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class UpdateScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository,
    private val reminderManager: ReminderManager
) {
    suspend operator fun invoke(schedule: Schedule) {
        val oldSchedule = repository.getScheduleById(schedule.id)
        if (oldSchedule != null) {
            reminderManager.cancelReminder(oldSchedule)
        }
        repository.updateSchedule(schedule)
        reminderManager.scheduleReminder(schedule)
    }
}
