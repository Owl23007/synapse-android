package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class UpdateScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(schedule: Schedule) {
        repository.updateSchedule(schedule)
    }
}
