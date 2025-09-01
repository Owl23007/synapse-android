package top.contins.synapse.domain.entity;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
