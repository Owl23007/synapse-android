package top.contins.synapse.feature.schedule.ui

import androidx.compose.ui.graphics.Color

enum class CalendarViewType {
    MONTH, WEEK, DAY
}

enum class EventType(val displayName: String, val color: Color) {
    MEETING("会议", Color(0xFF2196F3)),
    PERSONAL("个人", Color(0xFF4CAF50)),
    WORK("工作", Color(0xFFFF9800)),
    STUDY("学习", Color(0xFF9C27B0)),
    ENTERTAINMENT("娱乐", Color(0xFFE91E63))
}
