package top.contins.synapse.domain.repository

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.Goal

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: String): Goal?
    suspend fun createGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(id: String)
}
