package top.contins.synapse.domain.repository

import top.contins.synapse.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getSchedules(): Flow<List<Schedule>>
}
