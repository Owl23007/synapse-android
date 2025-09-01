package top.contins.synapse.domain.entity;

import java.util.Date;

/**
 * 任务实体类
 */
public class Task {
    private String id;
    private String title;
    private String description;
    private Date dueDate;
    private TaskStatus status;
    private TaskPriority priority;

    public Task() {}

    public Task(String id, String title, String description, Date dueDate, TaskStatus status, TaskPriority priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
}
