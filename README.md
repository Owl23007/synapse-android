# Synapse - 神经元 (智能日程助手)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange.svg)](https://dagger.dev/hilt/)

[English Version](README_EN.md) | **中文文档**

**Synapse** 是一款专为学生群体打造的智能日程管理助手，旨在弥合刚性日历工具与自然人类意图之间的鸿沟。通过整合任务、日程、目标和每日视图，通过“助手(Assistant)”进行自然语言交互，帮助用户高效规划学术与生活。

## ✨ 核心特性

- **🧠 智能交互 (NLP)**: 支持自然语言对话式录入（例如：“下周三下午3点数学作业截止”），自动解析为结构化日程。
- **📅 多维视图**:
  - **Schedule**: 支持月/周/日视图，集成农历与节气 (Solar Terms/Lunar Calendar)。
  - **Today**: 每日行动看板，聚焦当下。
  - **Task**: 待办事项清单管理。
  - **Goal**: 长期目标追踪与拆解。
- **🏗️ 模块化架构**: 基于 Clean Architecture + MVVM，严格的模块边界设计。

## 🛠️ 技术栈

本项目采用现代 Android 开发技术栈：

- **语言**: [Kotlin](https://kotlinlang.org/) (v2.2.10)
- **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
- **依赖注入**: [Hilt](https://dagger.dev/hilt/)
- **异步处理**: Coroutines & Flow
- **本地数据**: Room Database, DataStore
- **网络请求**: Retrofit, OkHttp
- **日历逻辑**: Biweekly, Lunar (针对农历优化)

## 📂 项目架构

项目遵循 **Clean Architecture** 原则，模块化结构如下：

| 模块 | 说明 | 依赖关系 |
|---|---|---|
| `:app` | 应用入口，DI 根节点，导航配置 | Depends on `:feature`, `:data` |
| `:domain` | **核心业务逻辑** (纯 Kotlin)，包含 Model 和 UseCases | 无 Android 依赖 |
| `:data` | 数据层实现 (Repository, DB, API) | Implements `:domain` |
| `:core:ui` | 公共 UI 组件与设计系统 | - |
| `:feature:*` | 功能模块 (Auth, Schedule, Task, Assistant...) | Depends on `:domain`, `:core:ui` |

### 依赖流向

`app` -> `feature` -> `domain` <- `data`
*注意：Feature 模块不直接依赖 Data 模块，必须通过 Domain 层接口交互。*

## 🚀 快速开始

### 环境要求

- JDK 17+
- Android Studio Ladybug 或更高版本

### 构建命令

```bash
# 构建 Debug 包
./gradlew assembleDebug
```

## 🤝 贡献指南

1. **分支管理**: 请基于 `master` 分支创建功能分支 `feature/your-feature-name`。
2. **代码风格**: 请遵循 Kotlin 官方编码规范。
3. **注释**: 核心业务逻辑请保留**中文注释**。
4. **提交规范**: 请使用中文描述，并使用 `git commit -m "feat/fix/docs/...(scope): 你的描述"` 提交。
