package top.contins.synapse.domain.model;

import java.util.Date;

/**
 * 目标实体类
 */
public class Goal {
    private String id;
    private String title;
    private String description;
    private Date startDate;
    private Date targetDate;
    private boolean isCompleted;
    private int progress; // 0-100

    public Goal() {}

    public Goal(String id, String title, String description, Date startDate, Date targetDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.isCompleted = false;
        this.progress = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) { this.targetDate = targetDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = Math.max(0, Math.min(100, progress)); }
}
