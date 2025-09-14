package top.contins.synapse.domain.model;

import java.util.Date;

/**
 * 日程实体类
 */
public class Schedule {
    private String id;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private String location;
    private ScheduleType type;

    public Schedule() {}

    public Schedule(String id, String title, String description, Date startTime, Date endTime, String location, ScheduleType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ScheduleType getType() { return type; }
    public void setType(ScheduleType type) { this.type = type; }
}
