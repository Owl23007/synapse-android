package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(schedule: Schedule) {
        repository.deleteSchedule(schedule)
    }

    suspend fun byId(id: String) {
        repository.deleteScheduleById(id)
    }
}
