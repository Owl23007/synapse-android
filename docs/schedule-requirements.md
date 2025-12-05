# 日程管理功能需求文档

> **项目**: Synapse 个人助手  
> **模块**: Schedule (日程/计划)  
> **版本**: v1.1  
> **创建日期**: 2025-12-05  
> **最后更新**: 2025-12-05  
> **状态**: 需求定义阶段

---

## 1. 概述

### 1.1 背景

Synapse 是一款基于日历 + AI Agent 赋能的个人助手应用。"计划"模块作为核心功能之一，需要为用户提供完善的日程管理能力，并为后续 AI 智能建议功能奠定数据基础。

### 1.2 目标

- 提供多视图日历展示，满足不同场景下的日程查看需求
- 实现日程的完整生命周期管理（创建、编辑、查看、删除）
- 支持日程提醒，确保用户不会错过重要事项
- 支持日历数据的导入导出，方便数据迁移和备份
- 支持网络日历订阅，整合外部日历源
- 融入中国特色，支持农历显示

### 1.3 用户画像

- 需要管理个人/工作日程的用户
- 希望通过 AI 辅助优化时间安排的用户
- 习惯使用农历的用户
- 需要整合多个日历源的用户

---

## 2. 功能需求

### 2.1 基本要求

#### 2.1.1 日历视图展示

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|---------|
| SCH-001 | 月视图展示 | P0 | 以月为单位展示日历，可左右滑动切换月份，当前日期高亮显示 |
| SCH-002 | 周视图展示 | P0 | 以周为单位展示日历，显示当周7天及各时段的日程安排 |
| SCH-003 | 日视图展示 | P0 | 以时间轴形式展示单日日程，按时间顺序排列，显示日程详情 |
| SCH-004 | 视图切换 | P0 | 支持月/周/日视图间的快速切换，可通过Tab或手势操作 |
| SCH-005 | 日程标记 | P1 | 在日历日期单元格上显示日程数量/颜色标记，便于快速识别 |
| SCH-006 | 日期跳转 | P1 | 支持快速跳转到指定日期，点击"今天"按钮回到当前日期 |

**视图详细说明**：

**月视图**：

```plaintext
┌─────────────────────────────────────────┐
│  < 2025年12月 >                 [周][日] │
├─────┬─────┬─────┬─────┬─────┬─────┬─────┤
│ 日  │ 一  │ 二   │ 三  │ 四   │ 五  │ 六  │
├─────┼─────┼─────┼─────┼─────┼─────┼─────┤
│     │  1  │  2  │  3  │  4  │  5  │  6  │
│     │初一 │     │ ●●  │     │ ●   │     │
├─────┼─────┼─────┼─────┼─────┼─────┼─────┤
│  7  │  8  │  9  │ 10  │ 11  │ 12  │ 13  │
│     │大雪 │     │     │     │     │     │
└─────┴─────┴─────┴─────┴─────┴─────┴─────┘
```

**周视图**：

```plaintext
┌────────────────────────────────────────────────┐
│  < 第49周 >                           [月][日] │
├──────┬──────┬──────┬──────┬──────┬──────┬──────┤
│ 12/1 │ 12/2 │ 12/3 │ 12/4 │ 12/5 │ 12/6 │ 12/7 │
├──────┼──────┼──────┼──────┼──────┼──────┼──────┤
│08:00 │      │      │██████│      │      │      │
│09:00 │██████│      │██████│      │      │      │
│10:00 │██████│      │      │      │██████│      │
│...   │      │      │      │      │      │      │
└──────┴──────┴──────┴──────┴──────┴──────┴──────┘
```

**日视图**：

```plaintext
┌─────────────────────────────────────────────┐
│  2025年12月5日 星期五               [月][周]  │
│  十一月初五 大雪后第3天                       │
├─────────────────────────────────────────────┤
│ 08:00 ┌────────────────────────────┐        │
│       │ 📅 团队周会                │        │
│ 09:00 │ 会议室A · 工作             │        │
│       └────────────────────────────┘        │
│ 10:00                                       │
│ 11:00 ┌────────────────────────────┐        │
│       │ 📚 学习 Kotlin 协程        │        │
│ 12:00 └────────────────────────────┘        │
│ ...                                         │
└─────────────────────────────────────────────┘
```

#### 2.1.2 日程 CRUD 操作

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|---------|
| SCH-010 | 创建日程 | P0 | 点击日期或浮动按钮，打开日程编辑界面，填写信息后保存 |
| SCH-011 | 查看日程 | P0 | 点击日程卡片，展示日程完整详情（标题、时间、地点、描述等） |
| SCH-012 | 编辑日程 | P0 | 在详情页点击编辑按钮，进入编辑模式修改日程信息 |
| SCH-013 | 删除日程 | P0 | 支持滑动删除或详情页删除按钮，删除前需二次确认 |
| SCH-014 | 快速创建 | P1 | 长按时间段快速创建该时段的日程，减少操作步骤 |

**日程数据字段**：

| 字段名 | 类型 | 必填 | 说明 |
|-------|------|-----|------|
| id | String | 是 | 唯一标识符 (UUID) |
| title | String | 是 | 日程标题，最大100字符 |
| description | String | 否 | 日程描述，最大1000字符 |
| startTime | DateTime | 是 | 开始时间（UTC） |
| endTime | DateTime | 是 | 结束时间（UTC） |
| timezoneId | String | 是 | 时区标识（如 `Asia/Shanghai`） |
| location | String | 否 | 地点信息 |
| type | ScheduleType | 是 | 日程类型（会议/个人/工作/学习/娱乐） |
| color | Color | 否 | 自定义颜色，默认跟随类型 |
| reminderMinutes | List | 否 | 提前提醒时间列表（分钟） |
| repeatRule | RepeatRule | 否 | 重复规则 |
| calendarId | String | 是 | 所属日历ID |
| isAllDay | Boolean | 否 | 是否全天事件 |
| isFromSubscription | Boolean | 否 | 是否来自订阅日历（只读） |
| createdAt | DateTime | 是 | 创建时间 |
| updatedAt | DateTime | 是 | 最后更新时间 |

**日程类型枚举**：

```kotlin
enum class ScheduleType(val displayName: String, val defaultColor: Long) {
    MEETING("会议", 0xFF2196F3),      // 蓝色
    PERSONAL("个人", 0xFF4CAF50),     // 绿色
    WORK("工作", 0xFFFF9800),         // 橙色
    STUDY("学习", 0xFF9C27B0),        // 紫色
    ENTERTAINMENT("娱乐", 0xFFE91E63) // 粉色
}
```

**重复规则数据模型**（完整 RFC 5545 支持）：

```kotlin
data class RepeatRule(
    val frequency: Frequency,         // 重复频率
    val interval: Int = 1,            // 间隔（如每2周）
    val until: DateTime? = null,      // 结束日期（与 count 互斥）
    val count: Int? = null,           // 重复次数（与 until 互斥）
    val byDay: List<WeekdayNum>? = null,    // 每周几（支持 "2FR" 表示第2个周五）
    val byMonthDay: List<Int>? = null,      // 每月几号（1-31，-1 表示最后一天）
    val byMonth: List<Int>? = null,         // 每年几月（1-12）
    val bySetPos: List<Int>? = null,        // 位置过滤（如 -1 表示最后一个）
    val weekStart: Weekday = Weekday.MON    // 每周起始日
)

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
```

**重复规则示例**：

| 场景 | RRULE | RepeatRule 表示 |
|------|-------|----------------|
| 每天 | `FREQ=DAILY` | `frequency=DAILY` |
| 每工作日 | `FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR` | `frequency=WEEKLY, byDay=[MON,TUE,WED,THU,FRI]` |
| 每月15号 | `FREQ=MONTHLY;BYMONTHDAY=15` | `frequency=MONTHLY, byMonthDay=[15]` |
| 每月最后一天 | `FREQ=MONTHLY;BYMONTHDAY=-1` | `frequency=MONTHLY, byMonthDay=[-1]` |
| 每月第二个周五 | `FREQ=MONTHLY;BYDAY=2FR` | `frequency=MONTHLY, byDay=[WeekdayNum(FRI, 2)]` |
| 每月最后一个周五 | `FREQ=MONTHLY;BYDAY=-1FR` | `frequency=MONTHLY, byDay=[WeekdayNum(FRI, -1)]` |
| 每年1月1日 | `FREQ=YEARLY;BYMONTH=1;BYMONTHDAY=1` | `frequency=YEARLY, byMonth=[1], byMonthDay=[1]` |

#### 2.1.3 日程提醒功能

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|---------|
| SCH-020 | 提醒时间设置 | P0 | 支持设置多个提醒时间点（如提前5分钟、15分钟、1小时等） |
| SCH-021 | 系统通知 | P0 | 在提醒时间到达时，通过系统通知提醒用户 |
| SCH-022 | 通知交互 | P1 | 点击通知可直接跳转到日程详情页 |
| SCH-023 | 提醒恢复 | P1 | 设备重启后自动恢复所有待生效的提醒 |
| SCH-024 | 免打扰时段 | P2 | 支持设置免打扰时间段，该时段内不发送提醒通知 |

#### 2.1.4 时区处理（关键）

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|----------|
| SCH-060 | 时区存储 | P0 | 所有 `startTime`/`endTime` 存储为 **UTC 时间**，同时记录原始时区 |
| SCH-061 | 跨时区显示 | P0 | 设备时区变化时，日程显示时间自动转换为当地时间 |
| SCH-062 | 全天事件处理 | P0 | 全天事件不绑定具体时区，始终显示为本地日期 |
| SCH-063 | 时区选择 | P1 | 创建日程时可手动指定时区（默认使用设备时区） |

**技术实现**：

- 使用 `java.time.ZonedDateTime` 进行时间处理
- 数据库存储：`start_time_utc`（Long，毫秒时间戳）+ `timezone_id`（String，如 "Asia/Shanghai"）
- 显示时根据设备当前时区转换

**场景示例**：
> 用户在北京创建「09:00 团队会议」（Asia/Shanghai），飞往纽约后，日程应显示为前一天「20:00 团队会议」（America/New_York）

#### 2.1.5 日程冲突检测

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|----------|
| SCH-070 | 自动冲突检测 | P1 | 创建/编辑日程时，自动检测与现有日程的时间重叠 |
| SCH-071 | 冲突提示 | P1 | 检测到冲突时，高亮显示冲突日程，提供"仍要创建"或"取消"选项 |
| SCH-072 | 冲突日程查看 | P2 | 点击冲突提示可直接查看冲突的日程详情 |

**交互设计**：

```plaintext
┌─────────────────────────────────────┐
│ ⚠️ 检测到时间冲突                    │
├─────────────────────────────────────┤
│ 您已有以下日程：                      │
│ 📅 团队周会 09:00-10:00             │
│                                     │
│ 是否仍要创建新日程？                  │
│                                     │
│ [取消]              [仍要创建]       │
└─────────────────────────────────────┘
```

#### 2.1.6 多日历账户管理

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|----------|
| SCH-080 | 创建本地日历 | P1 | 支持创建多个本地日历（如"工作"、"个人"、"家庭"） |
| SCH-081 | 日历属性设置 | P1 | 每个日历可设置名称、颜色、默认提醒时间 |
| SCH-082 | 日历显示控制 | P1 | 支持勾选显示/隐藏特定日历的日程 |
| SCH-083 | 默认日历设置 | P2 | 可设置默认日历，快速创建时使用 |
| SCH-084 | 日历删除 | P2 | 删除日历时提示是否同时删除该日历下的所有日程 |

**日历数据模型**：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | String | 日历唯一标识 |
| name | String | 日历名称 |
| color | Long | 日历颜色 |
| isVisible | Boolean | 是否显示 |
| isDefault | Boolean | 是否为默认日历 |
| defaultReminderMinutes | List<Int> | 默认提醒时间 |
| createdAt | DateTime | 创建时间 |

**UI 设计**：

```plaintext
┌─────────────────────────────┐
│ 📅 我的日历                  │
├─────────────────────────────┤
│ ☑ 🔵 工作日历               │
│ ☑ 🟢 个人日历               │
│ ☐ 🟠 家庭日历               │
├─────────────────────────────┤
│ 📡 订阅日历                  │
├─────────────────────────────┤
│ ☑ 🔴 中国法定节假日          │
│ ☑ 🟣 NBA赛程                │
└─────────────────────────────┘
```

**预设提醒时间选项**：

- 准时 (0分钟)
- 提前5分钟
- 提前15分钟
- 提前30分钟
- 提前1小时
- 提前2小时
- 提前1天
- 自定义时间

**通知内容格式**：

```plaintext
┌─────────────────────────────────┐
│ 🔔 Synapse · 日程提醒           │
├─────────────────────────────────┤
│ 团队周会                        │
│ 15分钟后开始 · 会议室A           │
│                                 │
│ [查看详情]  [延后15分钟]         │
└─────────────────────────────────┘
```

---

### 2.2 扩展要求

#### 2.2.1 日历事件导入导出

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|---------|
| SCH-030 | 导出为 iCalendar | P1 | 支持将日程导出为标准 .ics 文件格式 |
| SCH-031 | 导出范围选择 | P1 | 支持选择导出全部日程或指定时间范围的日程 |
| SCH-032 | 导出分享 | P1 | 导出后可通过系统分享功能发送到其他应用 |
| SCH-033 | 导入 iCalendar | P1 | 支持从 .ics 文件导入日程数据 |
| SCH-034 | 导入预览 | P2 | 导入前展示待导入日程列表，用户可选择性导入 |
| SCH-035 | 冲突处理 | P2 | 导入时检测时间冲突，提示用户处理方式 |

**iCalendar 格式支持**（基于 RFC 5545）：

```ics
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Synapse//Calendar//CN
BEGIN:VEVENT
UID:uuid-123456@synapse
DTSTART:20251205T090000
DTEND:20251205T100000
SUMMARY:团队周会
DESCRIPTION:每周例行会议
LOCATION:会议室A
CATEGORIES:MEETING
RRULE:FREQ=WEEKLY;BYDAY=FR
BEGIN:VALARM
TRIGGER:-PT15M
ACTION:DISPLAY
END:VALARM
END:VEVENT
END:VCALENDAR
```

#### 2.2.2 网络订阅功能

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|---------|
| SCH-040 | 添加订阅源 | P2 | 支持通过 URL 添加 iCalendar 订阅源 |
| SCH-041 | 订阅管理 | P2 | 支持查看、编辑（名称/颜色）、删除订阅源 |
| SCH-042 | 自动同步 | P2 | 后台定期同步订阅源内容，可配置同步频率 |
| SCH-043 | 手动刷新 | P2 | 支持手动触发单个或全部订阅源刷新 |
| SCH-044 | 只读标识 | P2 | 订阅日程显示只读标识，不可编辑/删除 |

**常见订阅源示例**：

- Google Calendar 公开日历
- 中国法定节假日日历
- 体育赛事日历
- 学校课程表

**订阅数据模型**：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | String | 订阅ID |
| name | String | 订阅名称 |
| url | String | 订阅 URL |
| color | Color | 日程显示颜色 |
| syncInterval | Int | 同步间隔（小时） |
| lastSyncAt | DateTime | 最后同步时间 |
| isEnabled | Boolean | 是否启用 |

#### 2.2.3 农历相关实现

| 需求编号 | 需求描述 | 优先级 | 验收标准 |
|---------|---------|--------|---------|
| SCH-050 | 农历日期显示 | P1 | 在日历视图中显示农历日期（如"初一"、"十五"） |
| SCH-051 | 节气显示 | P1 | 二十四节气当天显示节气名称替代农历日期 |
| SCH-052 | 传统节日 | P1 | 显示春节、端午、中秋等传统节日标识 |
| SCH-053 | 公历节日 | P2 | 显示元旦、劳动节、国庆节等公历节日 |
| SCH-054 | 农历日程创建 | P2 | 支持按农历日期创建重复日程（如每年农历正月初一） |

**农历显示规则**：

1. 初一显示月份名（如"十一月"）
2. 其他日期显示日（如"初五"、"廿三"）
3. 节气日优先显示节气名
4. 传统节日优先级最高

**支持的传统节日**：

- 春节（正月初一）
- 元宵节（正月十五）
- 清明节（节气）
- 端午节（五月初五）
- 七夕节（七月初七）
- 中元节（七月十五）
- 中秋节（八月十五）
- 重阳节（九月初九）
- 腊八节（腊月初八）
- 除夕（腊月三十/廿九）

---

## 3. 非功能需求

### 3.1 性能要求

| 需求编号 | 需求描述 | 指标 |
|---------|---------|------|
| NFR-001 | 日历视图加载 | 月视图切换时间 < 100ms |
| NFR-002 | 日程列表加载 | 单日日程列表加载 < 200ms |
| NFR-003 | 日程搜索 | 搜索响应时间 < 500ms |
| NFR-004 | 导入性能 | 1000条日程导入 < 5s |

### 3.2 存储要求

| 需求编号 | 需求描述 | 说明 |
|---------|---------|------|
| NFR-010 | 本地存储优先 | 所有日程数据优先存储在本地 Room 数据库 |
| NFR-011 | 数据量支持 | 支持存储至少 10,000 条日程记录 |
| NFR-012 | 离线可用 | 无网络时仍可正常使用所有本地功能 |

### 3.3 兼容性要求

| 需求编号 | 需求描述 | 说明 |
|---------|---------|------|
| NFR-020 | Android 版本 | 支持 Android 10 (API 29) 及以上 |
| NFR-021 | 屏幕适配 | 支持手机竖屏、横屏及平板设备 |
| NFR-022 | 深色模式 | 支持系统深色模式切换 |

### 3.4 安全要求

| 需求编号 | 需求描述 | 说明 |
|---------|---------|------|
| NFR-030 | 数据加密 | 敏感数据本地加密存储 |
| NFR-031 | 权限最小化 | 仅申请必要的系统权限 |

### 3.5 数据备份与恢复

| 需求编号 | 需求描述 | 说明 |
|---------|---------|------|
| NFR-040 | 自动云备份 | 使用 Android Backup Service 自动备份到 Google Drive |
| NFR-041 | 手动完整导出 | 支持导出为 JSON 文件（含日程、订阅源、设置） |
| NFR-042 | 数据恢复 | 重装 App 后可从云端或本地文件恢复数据 |
| NFR-043 | 增量备份 | 仅备份自上次备份后的变更数据 |

**JSON 导出格式**：

```json
{
  "version": "1.0",
  "exportTime": "2025-12-05T10:30:00Z",
  "calendars": [...],
  "schedules": [...],
  "subscriptions": [...],
  "settings": {
    "defaultCalendarId": "...",
    "doNotDisturbStart": "22:00",
    "doNotDisturbEnd": "08:00"
  }
}
```

### 3.6 无障碍支持（Accessibility）

| 需求编号 | 需求描述 | 说明 |
|---------|---------|------|
| NFR-050 | TalkBack 支持 | 日历视图、日程卡片支持屏幕阅读器 |
| NFR-051 | 语音描述完整 | 所有交互元素有完整的 contentDescription |
| NFR-052 | 触摸目标尺寸 | 可点击区域不小于 48dp × 48dp |

**语音描述示例**：

- 日期单元格："12月5日，星期五，有2个日程"
- 日程卡片："团队周会，09:00 到 10:00，会议室A，工作类型"
- 添加按钮："添加新日程"

### 3.7 国际化（i18n）

| 需求编号 | 需求描述 | 说明 |
|---------|---------|------|
| NFR-060 | 多语言支持 | 支持简体中文（默认）、英文 |
| NFR-061 | 日期格式本地化 | 根据系统语言显示日期（en-US: "Dec 5", zh-CN: "12月5日"） |
| NFR-062 | 数字格式本地化 | 数字、时间格式跟随系统区域设置 |
| NFR-063 | RTL 布局支持 | 预留从右到左语言支持（阿拉伯语等） |

---

## 4. 技术方案概要

### 4.1 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| UI 层 | Jetpack Compose | 声明式 UI |
| 日历组件 | kizitonwose/calendar-compose 2.6.0 | 日历视图渲染 |
| 架构模式 | MVVM + Clean Architecture | 分层架构 |
| 依赖注入 | Hilt 2.57.1 | DI 框架 |
| 本地存储 | Room 2.6.1 | SQLite ORM |
| 异步处理 | Kotlin Coroutines + Flow | 响应式编程 |
| iCalendar | biweekly 0.6.8 | 日历格式解析 |
| 农历 | cn.6tail:lunar 1.7.3 | 农历转换（支持节气交接时刻） |
| 提醒调度 | AlarmManager + WorkManager | 精确/后台任务 |

### 4.2 模块划分

```plaintext
feature/schedule/          # 日程功能模块
├── ui/
│   ├── ScheduleScreen.kt      # 主屏幕
│   ├── CalendarViews.kt       # 月/周/日视图
│   ├── ScheduleEditSheet.kt   # 编辑弹窗
│   ├── ScheduleDetailScreen.kt # 详情页
│   └── SubscriptionScreen.kt  # 订阅管理
├── viewmodel/
│   └── ScheduleViewModel.kt   # 状态管理
└── util/
    ├── LunarDateHelper.kt     # 农历工具
    ├── ICalendarHelper.kt     # 导入导出
    └── ReminderScheduler.kt   # 提醒调度

domain/
├── model/
│   ├── Schedule.kt            # 日程实体
│   ├── CalendarSubscription.kt # 订阅实体
│   └── RepeatRule.kt          # 重复规则
├── repository/
│   ├── ScheduleRepository.kt
│   └── SubscriptionRepository.kt
└── usecase/
    ├── ScheduleCrudUseCase.kt
    ├── ImportExportUseCase.kt
    └── SyncSubscriptionUseCase.kt

data/
├── local/
│   ├── entity/
│   │   ├── ScheduleEntity.kt
│   │   └── SubscriptionEntity.kt
│   ├── dao/
│   │   ├── ScheduleDao.kt
│   │   └── SubscriptionDao.kt
│   └── AppDatabase.kt
└── repository/
    ├── ScheduleRepositoryImpl.kt
    └── SubscriptionRepositoryImpl.kt
```

### 4.3 数据库设计

**schedules 表**：

```sql
CREATE TABLE schedules (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    start_time_utc INTEGER NOT NULL,  -- UTC 时间戳（毫秒）
    end_time_utc INTEGER NOT NULL,    -- UTC 时间戳（毫秒）
    timezone_id TEXT NOT NULL,        -- 时区标识（如 Asia/Shanghai）
    location TEXT,
    type TEXT NOT NULL,
    color INTEGER,
    reminder_minutes TEXT,  -- JSON 数组 [5, 15, 60]
    repeat_rule TEXT,       -- JSON 对象（完整 RRULE）
    calendar_id TEXT NOT NULL,
    is_all_day INTEGER DEFAULT 0,
    is_from_subscription INTEGER DEFAULT 0,
    subscription_id TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (calendar_id) REFERENCES calendars(id) ON DELETE CASCADE
);

-- 时间范围查询索引
CREATE INDEX idx_schedules_time ON schedules(start_time_utc, end_time_utc);
-- 日历+时间复合索引（快速查询某日历的日程）
CREATE INDEX idx_schedules_calendar_time ON schedules(calendar_id, start_time_utc);
-- 订阅日程索引
CREATE INDEX idx_schedules_subscription ON schedules(subscription_id);
```

**calendars 表**：

```sql
CREATE TABLE calendars (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    color INTEGER NOT NULL,
    is_visible INTEGER DEFAULT 1,
    is_default INTEGER DEFAULT 0,
    default_reminder_minutes TEXT,  -- JSON 数组
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

**subscriptions 表**：

```sql
CREATE TABLE subscriptions (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    color INTEGER,
    sync_interval INTEGER DEFAULT 24,
    last_sync_at INTEGER,
    is_enabled INTEGER DEFAULT 1,
    created_at INTEGER NOT NULL
);
```

---

## 5. 开发计划

### 5.1 里程碑

| 阶段 | 内容 | 预估工期 |
|------|-----|---------|
| M1 | 数据层基础（Room + Repository） | 3天 |
| M2 | 月/周/日视图实现 | 5天 |
| M3 | 日程 CRUD 功能 | 4天 |
| M4 | 日程提醒系统 | 3天 |
| M5 | 农历功能集成 | 2天 |
| M6 | 导入导出功能 | 3天 |
| M7 | 网络订阅功能 | 3天 |
| M8 | 测试与优化 | 3天 |

**总计**: 约 26 个工作日

### 5.2 依赖关系

```plaintext
M1 (数据层) ──┬──> M2 (视图) ──> M5 (农历)
              │
              └──> M3 (CRUD) ──┬──> M4 (提醒)
                               │
                               └──> M6 (导入导出) ──> M7 (订阅)
```

---

## 6. 风险与缓解措施

| 风险 | 影响 | 缓解措施 |
|------|-----|----------|
| AlarmManager 在 Android 12+ 限制 | 提醒不准时 | 使用 `SCHEDULE_EXACT_ALARM` 权限，引导用户授权；采用分层提醒策略 |
| 国产机后台限制 | 提醒被杀 | WorkManager + 前台服务兜底；引导用户加入白名单 |
| iCalendar 格式兼容性 | 导入失败 | 使用成熟的 biweekly 库，做好异常处理和容错 |
| 订阅源不可访问 | 同步失败 | 增加指数退避重试机制，友好提示用户 |
| 大量日程性能问题 | 界面卡顿 | 分页加载，虚拟列表，数据库索引优化 |
| 跨时区显示混乱 | 用户困惑 | 统一 UTC 存储，显示时转换；全天事件特殊处理 |

### 6.1 提醒调度分层策略

针对 Android 系统限制和国产机省电策略，采用分层提醒机制：

```kotlin
// ReminderScheduler.kt 核心策略
fun scheduleReminder(schedule: Schedule, reminderMinutes: Int) {
    val triggerTime = schedule.startTime.minusMinutes(reminderMinutes.toLong())
    val delayMillis = triggerTime.toEpochMilli() - System.currentTimeMillis()
    
    when {
        // 1. 10分钟内：使用 AlarmManager 精确提醒
        delayMillis < 10 * 60 * 1000 -> {
            if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
                // 引导用户开启精确闹钟权限
                showExactAlarmPermissionDialog()
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.toEpochMilli(),
                    pendingIntent
                )
            }
        }
        // 2. 10分钟-24小时：使用 WorkManager 精确窗口
        delayMillis < 24 * 60 * 60 * 1000 -> {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_${schedule.id}_$reminderMinutes",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf("scheduleId" to schedule.id))
                    .build()
            )
        }
        // 3. 超过24小时：存储待调度，每日凌晨批量注册
        else -> {
            savePendingReminder(schedule.id, triggerTime)
        }
    }
}
```

**设备重启恢复**：

```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 重新注册所有未过期的提醒
            ReminderScheduler.rescheduleAllReminders(context)
        }
    }
}
```

---

## 7. 后续规划（AI 集成）

完成基础日程功能后，计划集成 AI 能力：

1. **自然语言创建日程**：用户说"帮我明天下午3点安排一个产品评审会议"，AI 解析并创建日程
2. **智能时间推荐**：AI 分析日程分布，推荐空闲时间段
3. **日程冲突检测**：创建/编辑时 AI 自动检测并提示时间冲突
4. **日程执行分析**：AI 分析日程完成情况，给出效率建议
5. **AI 确认模式**：AI 生成的建议需用户确认后才执行

---

## 附录

### A. 权限声明

```xml
<!-- 精确闹钟权限 (Android 12+) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- 通知权限 (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- 开机启动恢复提醒 -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- 网络访问（订阅同步） -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 前台服务（后台同步） -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### B. 参考资料

- [iCalendar RFC 5545](https://datatracker.ietf.org/doc/html/rfc5545)
- [kizitonwose/calendar](https://github.com/kizitonwose/calendar)
- [biweekly iCalendar 库](https://github.com/mangstadt/biweekly)
- [lunar 农历库](https://github.com/6tail/lunar-java)
- [Android AlarmManager 文档](https://developer.android.com/reference/android/app/AlarmManager)
- [Android WorkManager 文档](https://developer.android.com/topic/libraries/architecture/workmanager)

---

### C. 用户场景补充

#### 场景1：跨时区旅行
>
> 用户从北京飞往纽约，希望日程自动适应当地时间  
> **解决方案**：SCH-060~063（时区处理）

#### 场景2：法定节假日调休
>
> 中国节假日经常调休（如周日上班）  
> **解决方案**：
>
> - 在"中国法定节假日"订阅源中包含调休信息
> - 日历视图用特殊标记显示调休日（如"班"标识）

#### 场景3：设备无网络 + 重启
>
> 用户飞行模式下重启手机，提醒仍需触发  
> **解决方案**：
>
> - `BOOT_COMPLETED` 广播接收器重新注册提醒
> - 所有提醒数据持久化存储在本地数据库

#### 场景4：多角色日程管理
>
> 用户需要同时管理工作、个人、家庭等不同类型日程  
> **解决方案**：SCH-080~084（多日历账户管理）

---

> **文档版本历史**
>
> | 版本 | 日期 | 作者 | 变更说明 |
> |-----|------|-----|----------|
> | 1.0 | 2025-12-05 | - | 初始版本 |
> | 1.1 | 2025-12-05 | - | 新增：重复规则完整定义、时区处理、日程冲突检测、多日历账户、数据备份、无障碍支持、国际化、提醒分层策略 |
