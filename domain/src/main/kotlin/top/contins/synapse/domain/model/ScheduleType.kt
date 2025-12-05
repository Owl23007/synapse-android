package top.contins.synapse.domain.model

enum class ScheduleType(val displayName: String) {
    MEETING("会议"),
    PERSONAL("个人"),
    WORK("工作"),
    STUDY("学习"),
    ENTERTAINMENT("娱乐")
}
