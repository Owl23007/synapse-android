package top.contins.synapse.domain.usecase.calendar

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.CalendarAccount
import top.contins.synapse.domain.repository.CalendarRepository
import javax.inject.Inject

class GetCalendarsUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    operator fun invoke(): Flow<List<CalendarAccount>> {
        return repository.getAllCalendars()
    }

    suspend fun getById(id: String): CalendarAccount? {
        return repository.getCalendarById(id)
    }
}
