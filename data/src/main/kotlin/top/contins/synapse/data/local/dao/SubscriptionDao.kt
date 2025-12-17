package top.contins.synapse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.local.entity.SubscriptionEntity

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: String): @JvmSuppressWildcards SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): @JvmSuppressWildcards Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity): @JvmSuppressWildcards Int

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity): @JvmSuppressWildcards Int

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: String): @JvmSuppressWildcards Int
}
