package top.contins.synapse.domain.usecase.subscription

import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject
import java.util.UUID

/**
 * Create a new calendar subscription
 */
class CreateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    /**
     * Create a new subscription
     * @param name Display name for the subscription
     * @param url URL to the iCalendar feed
     * @param color Optional color for events from this subscription
     * @param syncInterval Sync interval in hours (default 24)
     * @return Created subscription ID
     */
    suspend operator fun invoke(
        name: String,
        url: String,
        color: Long? = null,
        syncInterval: Int = 24
    ): String {
        require(name.isNotBlank()) { "Subscription name cannot be empty" }
        require(url.isNotBlank()) { "Subscription URL cannot be empty" }
        require(syncInterval > 0) { "Sync interval must be positive" }
        
        // Validate URL format
        require(isValidUrl(url)) { "Invalid URL format" }
        
        val subscription = Subscription(
            id = UUID.randomUUID().toString(),
            name = name,
            url = url,
            color = color,
            syncInterval = syncInterval,
            lastSyncAt = null,
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )
        
        repository.insertSubscription(subscription)
        
        return subscription.id
    }
    
    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            uri.scheme in listOf("http", "https", "webcal")
        } catch (e: Exception) {
            false
        }
    }
}
