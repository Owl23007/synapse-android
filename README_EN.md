# Synapse

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange.svg)](https://dagger.dev/hilt/)

**English Version** | [中文文档](README.md)

**Synapse** is an intelligent schedule management assistant tailored for students, bridging the gap between rigid calendar tools and natural human intent. By integrating tasks, schedules, goals, and a daily dashboard "Today", combined with an AI Assistant for natural language interaction, it acts as a second brain for academic and daily life management.

## ✨ Key Features

- **🧠 Intelligent Interaction (NLP)**: Input plans via natural conversation (e.g., "Math assignment due next Wednesday at 3 PM"), automatically parsed into structured events.
- **📅 Multi-Dimensional Views**:
  - **Schedule**: Month/Week/Day views with Lunar Calendar and Solar Terms support.
  - **Today**: Daily dashboard focusing on immediate actions.
  - **Task**: Todo list management.
  - **Goal**: Long-term goal tracking.
- **🏗️ Modular Architecture**: Built on Clean Architecture + MVVM with strict module boundaries.

## 🛠️ Tech Stack

Built with modern Android development standards:

- **Language**: [Kotlin](https://kotlinlang.org/) (v2.2.10)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
- **DI**: [Hilt](https://dagger.dev/hilt/)
- **Async**: Coroutines & Flow
- **Local Data**: Room Database, DataStore
- **Network**: Retrofit, OkHttp
- **Calendar Logic**: Biweekly, Lunar (Localized optimization)

## 📂 Architecture

The project follows **Clean Architecture** principles:

| Module | Description | Dependencies |
|---|---|---|
| `:app` | Application entry, DI Root, Navigation | Depends on `:feature`, `:data` |
| `:domain` | **Core Business Logic** (Pure Kotlin), Models & UseCases | No Android dependencies |
| `:data` | Data Layer Implementation (Repository, DB, API) | Implements `:domain` |
| `:core:ui` | Shared UI components & Design System | - |
| `:feature:*` | Feature modules (Auth, Schedule, Task, Assistant...) | Depends on `:domain`, `:core:ui` |

### Dependency Flow

`app` -> `feature` -> `domain` <- `data`
*Note: Feature modules rely on Domain interfaces and do not depend on the Data module directly.*

## 🚀 Getting Started

### Prerequisites

- JDK 17+
- Android Studio Ladybug or newer

### Build Commands

```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test
```

## 🤝 Contribution

1. **Branching**: Create feature branches from `master` (e.g., `feature/your-feature-name`).
2. **Style**: Follow official Kotlin coding conventions.
3. **Comments**: Please maintain **Chinese comments** for core business logic as per project convention.
4. **Commit Messages**: Use Chinese descriptions and follow the format `git commit -m "feat/fix/docs/...(scope): your description"`.
