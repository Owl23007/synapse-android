package top.contins.synapse.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.contins.synapse.data.local.dao.SubscriptionDao
import top.contins.synapse.data.local.converter.DataMapper.toDomain
import top.contins.synapse.data.local.converter.DataMapper.toEntity
import top.contins.synapse.domain.model.Subscription
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) : SubscriptionRepository {

    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSubscriptionById(id: String): Subscription? {
        return subscriptionDao.getSubscriptionById(id)?.toDomain()
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription.toEntity())
    }

    override suspend fun updateSubscription(subscription: Subscription) {
        subscriptionDao.updateSubscription(subscription.toEntity())
    }

    override suspend fun deleteSubscription(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription.toEntity())
    }

    override suspend fun deleteSubscriptionById(id: String) {
        subscriptionDao.deleteSubscriptionById(id)
    }
}
