package top.contins.synapse.domain.usecase.subscription

import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * Delete a calendar subscription and all its associated schedules
 */
class DeleteSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * Delete subscription and all schedules from it
     * @param subscriptionId ID of subscription to delete
     */
    suspend operator fun invoke(subscriptionId: String) {
        // First delete all schedules from this subscription
        scheduleRepository.deleteSchedulesByCalendarId(subscriptionId)
        
        // Then delete the subscription itself
        subscriptionRepository.deleteSubscriptionById(subscriptionId)
    }
}
