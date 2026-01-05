package top.contins.synapse.domain.model.schedule

enum class ScheduleType(val displayName: String, val defaultColor: Long) {
    MEETING("会议", 0xFF2196F3),      // 蓝色
    PERSONAL("个人", 0xFF4CAF50),     // 绿色
    WORK("工作", 0xFFFF9800),         // 橙色
    STUDY("学习", 0xFF9C27B0),        // 紫色
    ENTERTAINMENT("娱乐", 0xFFE91E63), // 粉色
    EVENT("事项", 0xFF607D8B)         // 蓝灰色
}
