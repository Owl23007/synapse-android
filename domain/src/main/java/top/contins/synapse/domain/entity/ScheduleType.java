package top.contins.synapse.domain.entity;

/**
 * 日程类型枚举
 */
public enum ScheduleType {
    MEETING("会议"),
    PERSONAL("个人"),
    WORK("工作"),
    STUDY("学习"),
    ENTERTAINMENT("娱乐");

    private final String displayName;

    ScheduleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
