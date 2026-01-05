package top.contins.synapse.domain.model.task

enum class TaskPriority(val displayName: String) {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    URGENT("紧急")
}
