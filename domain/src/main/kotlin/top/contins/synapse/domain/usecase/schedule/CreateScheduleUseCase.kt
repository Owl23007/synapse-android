package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class CreateScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(schedule: Schedule) {
        // TODO: Add validation logic here (e.g. check for conflicts if needed)
        repository.insertSchedule(schedule)
    }
}
