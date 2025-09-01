package top.contins.synapse.data.repository;

import androidx.lifecycle.LiveData;
import top.contins.synapse.domain.entity.Task;
import java.util.List;

/**
 * 任务数据仓库接口
 */
public interface TaskRepository {
    LiveData<List<Task>> getAllTasks();
    LiveData<Task> getTaskById(String id);
    void insertTask(Task task);
    void updateTask(Task task);
    void deleteTask(String taskId);
    LiveData<List<Task>> getTasksByStatus(top.contins.synapse.domain.entity.TaskStatus status);
}
