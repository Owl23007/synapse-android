package top.contins.synapse.domain.repository

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.Subscription

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    suspend fun getSubscriptionById(id: String): Subscription?
    suspend fun insertSubscription(subscription: Subscription)
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(subscription: Subscription)
    suspend fun deleteSubscriptionById(id: String)
}
