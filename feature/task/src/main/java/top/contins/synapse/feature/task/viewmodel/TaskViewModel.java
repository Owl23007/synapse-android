package top.contins.synapse.feature.task.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import top.contins.synapse.data.repository.TaskRepository;
import top.contins.synapse.data.repository.TaskRepositoryImpl;
import top.contins.synapse.domain.entity.Task;
import top.contins.synapse.domain.entity.TaskStatus;
import java.util.List;

/**
 * 任务功能的ViewModel
 */
public class TaskViewModel extends ViewModel {
    private TaskRepository taskRepository;

    public TaskViewModel() {
        taskRepository = new TaskRepositoryImpl();
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskRepository.getAllTasks();
    }

    public LiveData<Task> getTaskById(String id) {
        return taskRepository.getTaskById(id);
    }

    public void addTask(Task task) {
        taskRepository.insertTask(task);
    }

    public void updateTask(Task task) {
        taskRepository.updateTask(task);
    }

    public void deleteTask(String taskId) {
        taskRepository.deleteTask(taskId);
    }

    public LiveData<List<Task>> getTasksByStatus(TaskStatus status) {
        return taskRepository.getTasksByStatus(status);
    }
}
