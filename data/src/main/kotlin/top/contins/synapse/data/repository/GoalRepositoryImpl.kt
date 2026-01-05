package top.contins.synapse.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.contins.synapse.data.local.converter.DataMapper.toDomain
import top.contins.synapse.data.local.converter.DataMapper.toEntity
import top.contins.synapse.data.local.dao.GoalDao
import top.contins.synapse.domain.model.goal.Goal
import top.contins.synapse.domain.repository.GoalRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {
    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGoalById(id: String): Goal? {
        return goalDao.getGoalById(id)?.toDomain()
    }

    override suspend fun createGoal(goal: Goal) {
        goalDao.insertGoal(goal.toEntity())
    }

    override suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal.toEntity())
    }

    override suspend fun deleteGoal(id: String) {
        goalDao.deleteGoalById(id)
    }
}
