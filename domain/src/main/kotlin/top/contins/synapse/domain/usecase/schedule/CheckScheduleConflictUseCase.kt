package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class CheckScheduleConflictUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(start: Long, end: Long): List<Schedule> {
        return repository.getConflictingSchedules(start, end)
    }
}
