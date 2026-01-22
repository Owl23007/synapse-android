package top.contins.synapse.domain.usecase.subscription

import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * Update subscription settings
 */
class UpdateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    /**
     * Update subscription name
     */
    suspend fun updateName(subscriptionId: String, name: String) {
        require(name.isNotBlank()) { "Name cannot be empty" }
        
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("Subscription not found")
        
        repository.updateSubscription(subscription.copy(name = name))
    }
    
    /**
     * Update subscription URL
     */
    suspend fun updateUrl(subscriptionId: String, url: String) {
        require(url.isNotBlank()) { "URL cannot be empty" }
        
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("Subscription not found")
        
        repository.updateSubscription(subscription.copy(url = url))
    }
    
    /**
     * Enable or disable subscription
     */
    suspend fun setEnabled(subscriptionId: String, enabled: Boolean) {
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("Subscription not found")
        
        repository.updateSubscription(subscription.copy(isEnabled = enabled))
    }
    
    /**
     * Update sync interval
     */
    suspend fun updateSyncInterval(subscriptionId: String, intervalHours: Int) {
        require(intervalHours > 0) { "Sync interval must be positive" }
        
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("Subscription not found")
        
        repository.updateSubscription(subscription.copy(syncInterval = intervalHours))
    }
}
