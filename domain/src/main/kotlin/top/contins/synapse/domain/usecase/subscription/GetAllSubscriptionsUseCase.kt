package top.contins.synapse.domain.usecase.subscription

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * Get all subscriptions
 */
class GetAllSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<List<Subscription>> {
        return repository.getAllSubscriptions()
    }
    
    suspend fun getById(id: String): Subscription? {
        return repository.getSubscriptionById(id)
    }
}
