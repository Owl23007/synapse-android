package top.contins.synapse.domain.model

enum class Frequency {
    DAILY,      // 每天
    WEEKLY,     // 每周
    MONTHLY,    // 每月
    YEARLY      // 每年
}

enum class Weekday { MON, TUE, WED, THU, FRI, SAT, SUN }

data class WeekdayNum(
    val weekday: Weekday,
    val ordinal: Int? = null  // null=每周，1=第一个，-1=最后一个
)

data class RepeatRule(
    val frequency: Frequency,         // 重复频率
    val interval: Int = 1,            // 间隔（如每2周）
    val until: Long? = null,          // 结束日期（时间戳），与 count 互斥
    val count: Int? = null,           // 重复次数（与 until 互斥）
    val byDay: List<WeekdayNum>? = null,    // 每周几（支持 "2FR" 表示第2个周五）
    val byMonthDay: List<Int>? = null,      // 每月几号（1-31，-1 表示最后一天）
    val byMonth: List<Int>? = null,         // 每年几月（1-12）
    val bySetPos: List<Int>? = null,        // 位置过滤（如 -1 表示最后一个）
    val weekStart: Weekday = Weekday.MON    // 每周起始日
)
