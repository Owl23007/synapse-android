package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ReminderManager
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository,
    private val reminderManager: ReminderManager
) {
    suspend operator fun invoke(schedule: Schedule) {
        reminderManager.cancelReminder(schedule)
        repository.deleteSchedule(schedule)
    }

    suspend fun byId(id: String) {
        val schedule = repository.getScheduleById(id)
        if (schedule != null) {
            reminderManager.cancelReminder(schedule)
        }
        repository.deleteScheduleById(id)
    }
}
