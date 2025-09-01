package top.contins.synapse.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import top.contins.synapse.domain.entity.Task;
import top.contins.synapse.domain.entity.TaskStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务数据仓库实现类
 */
public class TaskRepositoryImpl implements TaskRepository {
    private MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>(new ArrayList<>());

    @Override
    public LiveData<List<Task>> getAllTasks() {
        return tasksLiveData;
    }

    @Override
    public LiveData<Task> getTaskById(String id) {
        MutableLiveData<Task> taskLiveData = new MutableLiveData<>();
        List<Task> tasks = tasksLiveData.getValue();
        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getId().equals(id)) {
                    taskLiveData.setValue(task);
                    break;
                }
            }
        }
        return taskLiveData;
    }

    @Override
    public void insertTask(Task task) {
        List<Task> currentTasks = new ArrayList<>(tasksLiveData.getValue());
        currentTasks.add(task);
        tasksLiveData.setValue(currentTasks);
    }

    @Override
    public void updateTask(Task task) {
        List<Task> currentTasks = new ArrayList<>(tasksLiveData.getValue());
        for (int i = 0; i < currentTasks.size(); i++) {
            if (currentTasks.get(i).getId().equals(task.getId())) {
                currentTasks.set(i, task);
                break;
            }
        }
        tasksLiveData.setValue(currentTasks);
    }

    @Override
    public void deleteTask(String taskId) {
        List<Task> currentTasks = new ArrayList<>(tasksLiveData.getValue());
        currentTasks.removeIf(task -> task.getId().equals(taskId));
        tasksLiveData.setValue(currentTasks);
    }

    @Override
    public LiveData<List<Task>> getTasksByStatus(TaskStatus status) {
        MutableLiveData<List<Task>> filteredTasks = new MutableLiveData<>();
        List<Task> tasks = tasksLiveData.getValue();
        if (tasks != null) {
            List<Task> filtered = tasks.stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
            filteredTasks.setValue(filtered);
        }
        return filteredTasks;
    }
}
