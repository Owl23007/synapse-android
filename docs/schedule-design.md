# 日程模块设计与实现说明（基于当前工程）

> **项目**: Synapse 个人助手  
> **模块**: Schedule（日程/计划）  
> **版本**: v0.1（工程现状对齐版）  
> **日期**: 2025-12-18  
> **输入**: `docs/schedule-requirements.md`（v1.1） + 当前代码实现  

---

## 0. 范围与目标

本文档用于：

- 把 `docs/schedule-requirements.md` 的需求拆解到当前工程的模块与代码结构中
- 描述当前已实现/未实现的功能边界（避免“文档说有、代码没有”）
- 给出后续实现的落地方案（数据流、关键类、接口、查询策略、边界条件）

非目标：

- 不在本文中定义新的产品交互（以需求文档为准）
- 不替代详细 UI 设计稿

---

## 1. 需求分析（按优先级）

### 1.1 P0（必须）

- **日历视图**：月/周/日视图展示与切换（SCH-001~004）
- **日程生命周期**：创建/查看/编辑/删除（SCH-010~013）
- **提醒**：支持多个提醒点，系统通知可跳转（SCH-020~022）
- **时区（关键）**：UTC 存储 + 原始时区记录；设备时区变化时自动转换显示；全天事件特殊处理（SCH-060~062）

### 1.2 P1（重要）

- 日程标记（SCH-005）、日期跳转（SCH-006）
- 快速创建（SCH-014）
- 冲突检测与提示（SCH-070~071）
- 本地多日历管理（SCH-080~082）
- 农历日期显示/节气/传统节日（SCH-050~052）
- iCalendar 导入导出（SCH-030~033）

### 1.3 P2（增强）

- 免打扰时段（SCH-024）
- 冲突日程可跳转查看（SCH-072）
- 日历删除级联策略（SCH-084）
- iCalendar 导入预览/冲突处理（SCH-034~035）
- 网络订阅管理与同步（SCH-040~044）
- 农历重复日程（SCH-054）

### 1.4 非功能需求（NFR）落点

- 性能：月视图切换 < 100ms、单日列表 < 200ms（NFR-001~002）
- 本地存储优先：Room（NFR-010）
- 离线可用（NFR-012）
- Android 10+（API 29+）、深色模式、无障碍、i18n（NFR-020/022/050/060）

---

## 2. 当前工程现状（截至 2025-12-18）

### 2.1 技术栈与依赖（以工程为准）

- Kotlin `2.2.10`、JVM Target `17`
- Jetpack Compose（BOM `2025.08.01`）
- Hilt `2.57.1`
- Room `2.6.1`（KSP）
- 日历 UI：`com.kizitonwose.calendar:compose:2.6.0`
- iCalendar：`net.sf.biweekly:biweekly:0.6.8`
- 农历：`cn.6tail:lunar:1.3.11`（注意：需求文档写的是 `1.7.3`，以工程为准或后续统一版本）

依赖来源：`gradle/libs.versions.toml`。

### 2.2 模块结构（Clean + 多模块）

- UI/Feature：`feature/schedule`
- Domain：`domain`
- Data：`data`（Room/DAO/RepositoryImpl/DI）
- Network：`network`（订阅源后续会用到）

模块声明：`settings.gradle.kts`。

### 2.3 已实现内容（可运行/可用）

- **月视图（Month View）**

- 代码：`feature/schedule/.../ui/CalendarViews.kt` 的 `MonthView()`
- 组件：kizitonwose `HorizontalCalendar`
- 农历：使用 `Lunar.fromDate(date).dayInChinese` 显示农历日
- 日程标记：在日期格内以 1~3 个小点展示当日有日程（颜色取 `schedule.color` 或默认色）

- **日程 CRUD（基础版）**

- 新建：`AddScheduleDialog`（标题/描述/全天/起止日期时间/日历/类型）
- 保存：`ScheduleViewModel.createSchedule()` → `CreateScheduleUseCase` → `ScheduleRepositoryImpl` → `ScheduleDao.insertSchedule()`
- 删除：列表上可直接删除（目前无二次确认）

- **数据层（Room + Repository）**

- `schedules` 表、索引、与 `calendars` 外键级联（见 `data/local/entity/ScheduleEntity.kt`）
- `ScheduleDao` 支持：
  - 全量 Flow
  - 范围查询（start/end）
  - 冲突查询（overlap）
  - 搜索（title/description like）

- **日历（CalendarAccount）基础**

- `calendars` 表存在
- 数据库创建/打开时会插入一个默认日历 `id=default`（见 `data/di/DatabaseModule.kt`）

### 2.4 未实现或仅有骨架（需要补齐）

- **周/日视图**：`CalendarComponent.kt` 有 Week/Day 的“时间槽”骨架，但 `ScheduleScreen.kt` 当前仅月视图可用，且周/日视图展示尚未与真实数据绑定。
- **查看详情/编辑**：缺少详情页与编辑入口（目前只有新增对话框）。
- **删除二次确认**：需求 SCH-013。
- **提醒系统**：缺少 `AlarmManager/WorkManager` 调度、通知、重启恢复（SCH-020~023）。
- **时区关键逻辑**：
  - 当前保存了 `timezoneId`，但筛选与展示主要按 `ZoneId.systemDefault()` 处理，尚未实现“跨时区旅行”的一致显示规则。
  - 全天事件（SCH-062）目前仅有 `isAllDay` 字段，缺少“始终显示为本地日期”的专门存储/转换策略。
- **冲突检测交互**：DAO 有 overlap 查询，但 `CreateScheduleUseCase` 仍是 TODO（需求 SCH-070~071）。
- **导入导出/订阅同步/农历节气节日**：依赖已引入，但缺少 util、usecase、worker、UI。

---

## 3. 模块划分与职责（建议与现状对齐）

> 目标：让 UI 只关心状态与事件；业务规则集中在 domain；落库/解析/同步放在 data。

### 3.1 `feature/schedule`（展示与交互）

建议目录（当前已存在部分）：

- `ui/`
  - `ScheduleScreen`：入口页面（视图切换 + 日程列表）
  - `CalendarViews`：月视图实现（已完成）
  - 后续：`ScheduleDetailScreen`、`ScheduleEditScreen`（或 Sheet）
- `viewmodel/`
  - `ScheduleViewModel`：组合状态（选中日期、当前月份、日程列表、过滤结果）
- `util/`
  - 目前为空，需求中提到的 `ReminderScheduler`、`ICalendarHelper`、`LunarDateHelper` 可逐步落地

### 3.2 `domain`（业务模型与用例）

- `model/`
  - `Schedule`、`RepeatRule`、`ScheduleType`、`CalendarAccount`、`Subscription`
- `repository/`
  - `ScheduleRepository`、`CalendarRepository`、`SubscriptionRepository`
- `usecase/`
  - `schedule/*`：`GetSchedulesUseCase`、`CreateScheduleUseCase`、`UpdateScheduleUseCase`、`DeleteScheduleUseCase`
  - `calendar/*`：获取/创建/更新/删除日历
  - （后续补齐）提醒、冲突检测、导入导出、订阅同步

### 3.3 `data`（落地实现）

- `local/`
  - `entity/`：Room 实体（Schedule/Calendar/Subscription）
  - `dao/`：查询与增删改
  - `converter/DataMapper`：Entity ↔ Domain（Gson 序列化 JSON 字段）
- `repository/`
  - `ScheduleRepositoryImpl`：基于 DAO 的实现
- `di/`
  - `DatabaseModule`：提供 Room + 默认数据
  - `RepositoryModule`：绑定 Repository 接口与实现

---

## 4. 数据模型与存储设计

### 4.1 Domain 模型（关键字段）

- `Schedule`
  - `startTime`/`endTime`: Long（UTC 毫秒时间戳）
  - `timezoneId`: String（创建时区，示例：`Asia/Shanghai`）
  - `reminderMinutes`: `List&lt;Int&gt;`?（提前提醒分钟列表）
  - `repeatRule`: RepeatRule?（重复规则对象）
  - `isAllDay`: Boolean（全天事件标识）

- `RepeatRule`
  - 支持 Frequency、ByDay（含序号）、ByMonthDay（含 -1）、ByMonth 等

### 4.2 Room 表结构（现状）

- `schedules`：见 `data/local/entity/ScheduleEntity.kt`
  - `reminder_minutes`/`repeat_rule` 使用 JSON 字符串存储
  - 已有索引：时间索引、日历+时间复合索引、订阅索引

- `calendars`：默认日历会在 DB callback 里插入
- `subscriptions`：实体与 DAO 已存在（同步逻辑未实现）

### 4.3 映射与序列化

- `data/local/converter/DataMapper.kt` 使用 Gson
  - `reminderMinutes: List<Int>` ↔ JSON
  - `repeatRule: RepeatRule` ↔ JSON

建议：如果未来要支持 schema 版本化/迁移与更强类型安全，可评估改用 `kotlinx.serialization` 或 Room TypeConverter（但当前实现已可用）。

---

## 5. 功能实现方案（按需求逐步落地）

### 5.1 日历视图（SCH-001~006）

**现状**：月视图已实现；周/日视图有骨架但未接入真实数据。

**落地要点**：

- 月视图：继续沿用 `HorizontalCalendar`，并把“今天/跳转日期/标记点”作为稳定能力。
- 周/日视图：
  - 用“时间轴 + 日程块（按 start/end 计算高度与偏移）”来替代固定示例文本
  - 支持点击时间槽快速创建（SCH-014）

**查询策略（建议）**：

- UI 不应全量订阅所有日程再在内存筛选（当前 `ScheduleViewModel` 是“全量 + 过滤”）；应按日期/范围从 DAO 拿 Flow。
- DAO 的范围查询目前是：`start >= startRange AND end <= endRange`，这会漏掉跨天/跨范围的日程。
- 建议为“视图展示”新增 overlap 范围查询：`start_time_utc < endRange AND end_time_utc > startRange`，与冲突检测保持一致。

### 5.2 CRUD（SCH-010~013）

**新建**：已完成基础对话框。

**需要补齐**：

- 查看详情页（SCH-011）：点击列表项进入详情
- 编辑（SCH-012）：详情页进入编辑模式
- 删除二次确认（SCH-013）：删除前弹窗确认；订阅日程不可删除

**校验建议（domain/usecase 层）**：

- 标题非空、长度限制
- `start < end`
- `calendarId` 存在
- 全天事件：保存时固定为“日期范围”，不受时区变化影响（见 5.3）

### 5.3 时区处理（SCH-060~063，关键）

**目标**：统一 UTC 存储；显示按设备时区转换；全天事件只看“本地日期”。

**现状风险点**：

- 当前筛选与展示主要用 `ZoneId.systemDefault()`，未使用 `timezoneId` 做“原始时区”转换规则。

**建议实现策略**：

#### 5.3.1 非全天事件

- 存储：`startTime/endTime` 永远为 UTC epoch millis；同时记录 `timezoneId`。
- 显示：
  - 列表显示：把 `Instant.ofEpochMilli(startTime)` 转换到 `ZoneId.systemDefault()` 展示
  - 详情页可展示“创建时区时间”（用 `timezoneId`）与“当前本地时间”（用 systemDefault）两套信息（如果产品需要）

#### 5.3.2 全天事件（重点）

需求要求“全天事件不绑定具体时区，始终显示为本地日期”。仅靠 UTC epoch 很难保证跨时区不漂移。

推荐在 schema 上补充一个“日期型”存储（可选二选一）：

- 方案 A（推荐）：增加 `all_day_start_date`/`all_day_end_date`（`TEXT`，ISO-8601 `yyyy-MM-dd`）
- 方案 B：增加 `all_day_date` 单日（大多数全天事件）

当 `isAllDay = true`：

- 筛选归属日：按 `LocalDate` 字段判断
- 显示：直接显示日期，不做时区换算

> 注：当前工程还未做 migration（并且 `fallbackToDestructiveMigration()` 开启），开发阶段可以先按最小成本推进；上线前需补齐正式迁移策略。

### 5.4 冲突检测（SCH-070~071）

**现状**：DAO 已实现 overlap 冲突查询：

- `ScheduleDao.getConflictingSchedules(start, end)`

**建议落点**：

- 在 `CreateScheduleUseCase`/`UpdateScheduleUseCase` 中加入冲突检查
- 将结果返回给 UI：
  - 无冲突：直接写库
  - 有冲突：UI 弹出冲突提示，并让用户选择“仍要创建/取消”

### 5.5 提醒系统（SCH-020~023）

**现状**：未实现。

**建议结构**：

- `feature/schedule/util/ReminderScheduler`：调度门面（根据时间范围选择 AlarmManager/WorkManager）
- `data` 或 `app` 层：
  - `ReminderWorker`：WorkManager 执行通知
  - `BootReceiver`：重启恢复
  - `Notification`：通知渠道与点击跳转

调度分层策略可直接参考需求文档的伪代码（10分钟内 AlarmManager、24小时内 WorkManager、更久做批量注册）。

### 5.6 iCalendar 导入导出（SCH-030~035）

**现状**：`biweekly` 依赖已引入，逻辑未实现。

**建议**：

- `feature/schedule/util/ICalendarHelper` 或 domain/usecase：
  - Export：Schedule → VEVENT（SUMMARY/DESCRIPTION/LOCATION/CATEGORIES/RRULE/VALARM）
  - Import：VEVENT → Schedule（注意 timezone、重复规则、提醒）
- 引入“导入预览 + 冲突处理”作为 P2 增强。

### 5.7 网络订阅（SCH-040~044）

**现状**：Subscription 的 entity/dao/repository 已有；缺少同步与 UI。

**建议**：

- `SyncSubscriptionUseCase` + `WorkManager` 定期同步：
  1) 拉取订阅 URL（.ics）
  2) biweekly 解析 VEVENT
  3) upsert 到 `schedules` 表：`isFromSubscription=true`、写入 `subscriptionId`
  4) UI 层对订阅事件展示“只读”并禁用编辑/删除

### 5.8 农历（SCH-050~054）

**现状**：月视图显示了农历“日”文本。

**建议补齐**：

- 节气：优先显示节气名（替代农历日）
- 传统节日：最高优先级显示
- 公历节日：P2

可在 `feature/schedule/util/LunarDateHelper` 封装：输入 `LocalDate`，输出“要显示的文案 + 类型（节气/节日/农历日）”。

---

## 6. UI 与状态管理（MVVM）

### 6.1 状态

建议在 `ScheduleViewModel` 维护：

- `selectedDate: StateFlow<LocalDate>`
- `currentMonth: StateFlow<YearMonth>`
- `viewType: StateFlow<CalendarViewType>`（避免 UI 本地状态和业务状态割裂）
- `schedulesForVisibleRange: StateFlow<List<Schedule>>`（范围查询替代全量）
- `uiEvents`（一次性事件：弹窗、toast、导航）

### 6.2 交互

- 日期点击：刷新“选中日的日程列表”
- 日程点击：进入详情
- 时间槽长按：进入“快速创建”并预填日期/时间

---

## 7. 性能与可维护性建议

- **范围查询优先**：月/周/日视图使用 overlap 范围查询，避免全量订阅导致卡顿。
- **索引已具备**：`idx_schedules_time`、`idx_schedules_calendar_time` 能支撑高频查询。
- **大数据量优化**：日程列表可按天分页或按时间段分组（后续再做）。

---

## 8. 测试与验收建议

- Unit Test
  - `DataMapper` 的 JSON 序列化/反序列化（RepeatRule/ReminderMinutes）
  - 冲突检测用例（overlap 判断）
- Instrumentation Test
  - Room 查询：范围查询/冲突查询在真实 SQLite 行为
- UI Test（Compose）
  - 月视图切换、选中日期、列表展示

---

## 9. 当前“实现对齐清单”（简表）

- 已完成：月视图（含农历日）/新增日程/本地存储/默认日历
- 待补齐（P0）：周视图、日视图、详情/编辑、提醒系统、关键时区与全天事件
- 待补齐（P1/P2）：冲突提示交互、导入导出、订阅同步、节气节日完善、免打扰

---

## 附录：关键文件索引

- 需求文档：`docs/schedule-requirements.md`
- 入口 UI：`feature/schedule/src/main/kotlin/top/contins/synapse/feature/schedule/ui/ScheduleScreen.kt`
- 月视图：`feature/schedule/src/main/kotlin/top/contins/synapse/feature/schedule/ui/CalendarViews.kt`
- 新建弹窗：`feature/schedule/src/main/kotlin/top/contins/synapse/feature/schedule/ui/AddScheduleDialog.kt`
- ViewModel：`feature/schedule/src/main/kotlin/top/contins/synapse/feature/schedule/viewmodel/ScheduleViewModel.kt`
- Domain 模型：`domain/src/main/kotlin/top/contins/synapse/domain/model/Schedule.kt`
- DAO：`data/src/main/kotlin/top/contins/synapse/data/local/dao/ScheduleDao.kt`
- Repository：`data/src/main/kotlin/top/contins/synapse/data/repository/ScheduleRepositoryImpl.kt`
- 数据库与默认数据：`data/src/main/kotlin/top/contins/synapse/data/di/DatabaseModule.kt`
