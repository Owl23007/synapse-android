package top.contins.synapse.domain.usecase.subscription

import top.contins.synapse.domain.repository.CalendarRepository
import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * 删除日历订阅及其所有关联的日程
 */
class DeleteSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: ScheduleRepository,
    private val calendarRepository: CalendarRepository
) {
    /**
     * 删除订阅及其所有日程
     * @param subscriptionId 要删除的订阅 ID
     */
    suspend operator fun invoke(subscriptionId: String) {
        // 先删除此订阅的所有日程
        scheduleRepository.deleteSchedulesByCalendarId(subscriptionId)
        
        // 删除对应日历
        calendarRepository.deleteCalendarById(subscriptionId)

        // 然后删除订阅本身
        subscriptionRepository.deleteSubscriptionById(subscriptionId)
    }
}
