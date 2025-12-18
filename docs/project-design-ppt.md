# Synapse 项目软件设计说明（PPT 版）

> 面向“软件设计展示 PPT”的精简文档：每个一级标题可对应 1~2 页 Slide。

- 项目：Synapse（个人助手：日历 + AI Agent）
- 代码仓库：`android/`
- 日期：2025-12-18

---

## 1. 背景与目标

### 1.1 背景

Synapse 是一款基于日历能力与 AI Agent 赋能的个人助手应用。核心价值在于：

- 以“时间”为中心组织个人信息与任务
- 用 AI 辅助用户做更好的规划与执行（后续阶段）

### 1.2 目标（MVP → 可扩展）

- **MVP**：日程/任务/目标的本地管理、可靠展示与编辑
- **可扩展**：为 AI 能力（自然语言创建、智能推荐、执行分析）沉淀结构化数据

---

## 2. 需求分析（产品视角）

### 2.1 核心业务域

- **日程（Schedule）**：日历视图、日程 CRUD、提醒、时区、农历、导入导出、订阅
- **任务（Task）**：待办管理（状态、优先级、截止时间）
- **目标（Goal）**：长期目标与进度
- **账号/认证（Auth）**：登录与凭据管理
- **AI 助手（Assistant）**：与日程/任务/目标联动（后续）

### 2.2 优先级拆解（示例）

- P0：基础视图 + 核心 CRUD + 数据可靠性
- P1：效率能力（快速创建、冲突检测）、本地多日历、农历增强
- P2：导入预览/冲突处理、订阅同步、免打扰

> 日程模块详细需求与边界：见 [docs/schedule-requirements.md](docs/schedule-requirements.md) 与 [docs/schedule-design.md](docs/schedule-design.md)。

---

## 3. 技术选型（工程视角）

### 3.1 栈与关键依赖

- 语言：Kotlin（JVM Target 17）
- UI：Jetpack Compose（Compose BOM）
- 架构：MVVM + Clean Architecture（多模块分层）
- DI：Hilt
- 异步：Kotlin Coroutines + Flow
- 本地存储：Room（KSP）
- 网络：Retrofit + OkHttp（为订阅/云能力预留）
- 日历渲染：kizitonwose/calendar-compose
- iCalendar：biweekly
- 农历：cn.6tail/lunar

### 3.2 选型理由（可上 PPT）

- Compose：声明式 UI，状态驱动，适配深色模式/无障碍更自然
- Clean + 多模块：隔离依赖，提升可维护性与测试性
- Room + Flow：本地优先、离线可用、UI 自动响应数据变化

---

## 4. 架构设计（Clean 分层）

### 4.1 分层职责

- **feature（UI 层）**：页面/组件、状态渲染、用户交互事件；只依赖 domain
- **domain（业务层）**：业务模型、UseCase、Repository 接口；不依赖 Android 框架
- **data（数据层）**：Room/DAO、Repository 实现、映射/序列化、数据源整合
- **network（网络层）**：API 定义与网络拦截（为订阅/云端同步预留）
- **core（通用层）**：可复用 UI/组件/基础能力
- **app（组装层）**：应用入口，路由/导航，依赖注入汇总

### 4.2 依赖方向（单向）

- app → feature/* → domain
- app → data → domain
- data → network（可选）
- feature/* → core:ui（可选）

**原则**：domain 不依赖任何上层；feature 不直接触达 Room/Retrofit。

---

## 5. 项目结构（与当前仓库对齐）

### 5.1 模块列表

- app：应用壳与导航
- core:ui：通用 UI 组件
- domain：业务模型 + UseCase + Repository 接口
- data：Room + DAO + Repository 实现 + Hilt 模块
- network：网络 API 与 token provider
- feature:auth：认证相关 UI
- feature:profile：个人信息 UI
- feature:task：任务模块
- feature:goal：目标模块
- feature:schedule：日程模块
- feature:assistant：AI 助手入口（后续增强）

### 5.2 典型目录约定（以 schedule 为例）

- `feature/<module>/ui`：页面/组件
- `feature/<module>/viewmodel`：状态与交互
- `domain/model`：领域模型
- `domain/usecase/<module>`：用例
- `data/local`：Room 实体/DAO/数据库
- `data/repository`：Repository 实现

---

## 6. 数据设计（Room + 领域模型）

### 6.1 数据原则

- 本地优先：所有核心数据先落本地，保证离线可用
- 领域模型与存储模型分离：Entity ↔ Domain 映射
- 关键字段可追溯：如日程时区、订阅来源、创建/更新时间

### 6.2 核心实体（示例）

- Schedule（日程）
- CalendarAccount（日历账户/本地日历）
- Subscription（订阅源）
- Task / Goal

### 6.3 索引与查询策略（示例）

- 日程按时间范围查询（支持月/周/日视图）
- 日历 ID + 时间的复合索引，支持“某日历的某段时间”快速查询
- 冲突检测：overlap 查询（start < end2 且 end > start2）

---

## 7. 关键业务流程（可做成时序图/流程图）

### 7.1 典型数据流：创建日程

1. UI 收集字段（标题/起止时间/日历/类型…）
2. ViewModel 调用 CreateScheduleUseCase
3. UseCase 执行业务校验（如 start < end、冲突检测）
4. Repository 写入 Room
5. Flow 推送数据变化，UI 自动刷新

### 7.2 典型数据流：月视图展示

1. 用户切换月份/选择日期
2. ViewModel 更新选中日期/可见范围
3. 通过范围查询获取需要的日程集合
4. 月格内显示日程标记点；下方列表显示选中日的日程

---

## 8. 非功能性设计（可靠性/性能/安全）

### 8.1 性能

- 视图按范围查询，避免全量加载导致卡顿
- Room 索引优化高频查询
- 列表渲染采用 Compose LazyColumn

### 8.2 可靠性

- 本地存储优先，离线可用
-（日程模块）提醒系统需支持“重启恢复”（BootReceiver + 持久化）

### 8.3 安全

- 权限最小化：只申请必要权限（通知、精确闹钟、开机广播、网络等）
- 敏感数据建议加密存储（AndroidX Security Crypto 可选）

---

## 9. 测试策略（质量保障）

- Unit Test：UseCase 业务规则（冲突检测、时间校验）、映射序列化
- DB Test：Room 查询与索引行为（范围查询/冲突查询）
- UI Test：关键页面交互（切换视图、创建/删除、详情/编辑）

---

## 10. 风险与应对（PPT 推荐页）

- Android 12+ 精确闹钟限制：引导授权 + 分层调度策略
- 国产机后台限制：WorkManager +（必要时）前台服务兜底
- iCalendar 兼容性：使用成熟库 biweekly + 容错解析
- 大量日程性能：范围查询 + 索引 + 列表虚拟化
- 跨时区与全天事件复杂：统一 UTC + 设计明确的全天事件存储策略

---

## 11. 里程碑（从工程落地视角）

- M1：数据层基础（Room + Repository + 默认数据）
- M2：日程月/周/日视图打通
- M3：日程 CRUD 完整闭环（详情/编辑/删除确认）
- M4：提醒系统（通知 + 重启恢复）
- M5：农历增强（节气/节日优先级）
- M6：导入导出（.ics）
- M7：订阅同步（定时刷新、只读事件）
- M8：测试与性能优化

---

## 附录：与代码的对应关系

- 构建与模块：`settings.gradle.kts`、`gradle/libs.versions.toml`
- 日程需求： [docs/schedule-requirements.md](docs/schedule-requirements.md)
- 日程设计对齐： [docs/schedule-design.md](docs/schedule-design.md)
