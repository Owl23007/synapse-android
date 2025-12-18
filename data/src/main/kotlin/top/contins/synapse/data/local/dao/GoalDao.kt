package top.contins.synapse.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.local.entity.GoalEntity

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY targetDate ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: String): @JvmSuppressWildcards GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): @JvmSuppressWildcards Long

    @Update
    suspend fun updateGoal(goal: GoalEntity): @JvmSuppressWildcards Int

    @Delete
    suspend fun deleteGoal(goal: GoalEntity): @JvmSuppressWildcards Int

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String): @JvmSuppressWildcards Int
}
