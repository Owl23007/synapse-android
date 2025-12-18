package top.contins.synapse.domain.repository

import top.contins.synapse.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * 任务数据仓库接口
 */
interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
}
