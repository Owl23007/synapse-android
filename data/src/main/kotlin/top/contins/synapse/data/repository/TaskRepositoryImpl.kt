package top.contins.synapse.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.contins.synapse.data.local.converter.DataMapper.toDomain
import top.contins.synapse.data.local.converter.DataMapper.toEntity
import top.contins.synapse.data.local.dao.TaskDao
import top.contins.synapse.domain.model.Task
import top.contins.synapse.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * 任务数据仓库实现类
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun createTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(id: String) {
        taskDao.deleteTaskById(id)
    }
}
