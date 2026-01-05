package top.contins.synapse.domain.model

enum class TaskStatus(val displayName: String) {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消")
}
