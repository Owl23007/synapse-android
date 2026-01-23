package top.contins.synapse.domain.usecase.subscription

import top.contins.synapse.domain.repository.CalendarRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * 更新订阅设置
 */
class UpdateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
    private val calendarRepository: CalendarRepository
) {
    /**
     * 更新订阅名称
     */
    suspend fun updateName(subscriptionId: String, name: String) {
        require(name.isNotBlank()) { "名称不能为空" }
        
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("订阅不存在")
        
        repository.updateSubscription(subscription.copy(name = name))
        calendarRepository.getCalendarById(subscriptionId)?.let { calendar ->
            calendarRepository.updateCalendar(
                calendar.copy(
                    name = name,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    /**
     * 更新订阅 URL
     */
    suspend fun updateUrl(subscriptionId: String, url: String) {
        require(url.isNotBlank()) { "URL 不能为空" }
        
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("订阅不存在")
        
        repository.updateSubscription(subscription.copy(url = url))
    }
    
    /**
     * 启用或禁用订阅
     */
    suspend fun setEnabled(subscriptionId: String, enabled: Boolean) {
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("订阅不存在")
        
        repository.updateSubscription(subscription.copy(isEnabled = enabled))
    }
    
    /**
     * 更新同步间隔
     */
    suspend fun updateSyncInterval(subscriptionId: String, intervalHours: Int) {
        require(intervalHours > 0) { "同步间隔必须大于 0" }
        
        val subscription = repository.getSubscriptionById(subscriptionId)
            ?: throw IllegalArgumentException("订阅不存在")
        
        repository.updateSubscription(subscription.copy(syncInterval = intervalHours))
    }
}
