package top.contins.synapse.domain.usecase.calendar

import top.contins.synapse.domain.model.CalendarAccount
import top.contins.synapse.domain.repository.CalendarRepository
import javax.inject.Inject

class DeleteCalendarUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(calendar: CalendarAccount) {
        repository.deleteCalendar(calendar)
    }

    suspend fun byId(id: String) {
        repository.deleteCalendarById(id)
    }
}
