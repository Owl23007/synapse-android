package top.contins.synapse.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.local.entity.TaskEntity

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): @JvmSuppressWildcards TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): @JvmSuppressWildcards Long

    @Update
    suspend fun updateTask(task: TaskEntity): @JvmSuppressWildcards Int

    @Delete
    suspend fun deleteTask(task: TaskEntity): @JvmSuppressWildcards Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String): @JvmSuppressWildcards Int
}
