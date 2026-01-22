package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * Result of subscription sync operation
 */
data class SyncResult(
    val success: Boolean,
    val addedCount: Int,
    val updatedCount: Int,
    val removedCount: Int,
    val error: String? = null
)

/**
 * Sync calendar subscription from network
 */
class SyncSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * Sync a single subscription
     * @param subscriptionId Subscription ID to sync
     * @return Sync result with counts
     */
    suspend operator fun invoke(subscriptionId: String): SyncResult {
        return try {
            val subscription = subscriptionRepository.getSubscriptionById(subscriptionId)
                ?: return SyncResult(
                    success = false,
                    addedCount = 0,
                    updatedCount = 0,
                    removedCount = 0,
                    error = "Subscription not found"
                )
            
            if (!subscription.isEnabled) {
                return SyncResult(
                    success = false,
                    addedCount = 0,
                    updatedCount = 0,
                    removedCount = 0,
                    error = "Subscription is disabled"
                )
            }
            
            // Fetch iCalendar content from URL (will be done by network layer)
            val icsContent = fetchSubscriptionContent(subscription.url)
            
            // Delete old schedules from this subscription
            scheduleRepository.deleteSchedulesByCalendarId(subscription.id)
            
            // Parse and import new schedules
            val parsedSchedules = parseICalendarForSubscription(icsContent, subscription)
            
            var addedCount = 0
            parsedSchedules.forEach { schedule ->
                try {
                    scheduleRepository.insertSchedule(schedule)
                    addedCount++
                } catch (e: Exception) {
                    // Log error but continue
                }
            }
            
            // Update last sync time
            val updatedSubscription = subscription.copy(
                lastSyncAt = System.currentTimeMillis()
            )
            subscriptionRepository.updateSubscription(updatedSubscription)
            
            SyncResult(
                success = true,
                addedCount = addedCount,
                updatedCount = 0,
                removedCount = parsedSchedules.size,
                error = null
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                addedCount = 0,
                updatedCount = 0,
                removedCount = 0,
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    /**
     * Sync all enabled subscriptions
     */
    suspend fun syncAll(): Map<String, SyncResult> {
        val results = mutableMapOf<String, SyncResult>()
        // This will be implemented with Flow collection
        return results
    }
    
    private suspend fun fetchSubscriptionContent(url: String): String {
        // Will be implemented with actual HTTP client
        return ""
    }
    
    private fun parseICalendarForSubscription(
        icsContent: String,
        subscription: Subscription
    ): List<top.contins.synapse.domain.model.schedule.Schedule> {
        // Will be implemented with biweekly library
        return emptyList()
    }
}
