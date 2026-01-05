package top.contins.synapse.domain.usecase.calendar

import top.contins.synapse.domain.model.schedule.CalendarAccount
import top.contins.synapse.domain.repository.CalendarRepository
import javax.inject.Inject

class UpdateCalendarUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(calendar: CalendarAccount) {
        repository.updateCalendar(calendar)
    }
}
