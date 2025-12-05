package top.contins.synapse.data.repository

import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor() : ScheduleRepository {
    override fun getSchedules(): Flow<List<Schedule>> {
        // TODO: Implement real data source
        return flowOf(emptyList())
    }
}
