package top.contins.synapse.domain.usecase.subscription

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * 获取所有订阅
 */
class GetAllSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    /**
     * 获取所有订阅列表
     */
    operator fun invoke(): Flow<List<Subscription>> {
        return repository.getAllSubscriptions()
    }
    
    /**
     * 根据 ID 获取订阅
     */
    suspend fun getById(id: String): Subscription? {
        return repository.getSubscriptionById(id)
    }
}
