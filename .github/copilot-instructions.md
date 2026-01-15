# Synapse Android Project - AI Agent Instructions

## Project Overview
This is a multi-module Android application using **Clean Architecture** and **MVVM** patterns.
- **Language:** Kotlin (primary), Java (Application class only).
- **UI:** Jetpack Compose (Material3).
- **DI:** Hilt.
- **Async:** Coroutines & Flow.
- **Data:** Room, DataStore.
- **Network:** Retrofit.

## Architecture & Modules
The project is strictly modularized. AI Agents must respect module boundaries.

### Module Structure
- **`app`**: Application entry point, DI root (`@HiltAndroidApp`), generic UI navigation (`MainEntry.kt`).
- **`domain`**: Pure Kotlin module. **NO Android dependencies**. Contains:
  - `model/`: Domain data classes.
  - `repository/`: Repository interfaces.
  - `usecase/`: Business logic (Single Responsibility).
- **`data`**: implementation of `domain` interfaces. Contains:
  - `local/`: Room Database, DAOs, Entities.
  - `repository/`: Repository implementations (maps Entities <-> Domain Models).
  - `di/`: Hilt modules for Data layer.
- **`feature/*`**: Feature specific modules (e.g., `:feature:schedule`). Contains:
  - `ui/`: Compose screens and components.
  - `viewmodel/`: `@HiltViewModel` classes.
- **`core/ui`**: Shared UI components and Desgin System.
- **`network`**: Network communication logic.

### Dependency Flow
`app` -> `feature/*` -> `domain`
`app` -> `data` -> `domain`
`feature` DOES NOT depend on `data` (direct dependency forbidden, must go through `domain`).

## Key Development Patterns

### 1. Domain Layer (Pure Kotlin)
- **UseCases**: Implement single business actions.
- **Naming**: `VerbSubjectUseCase` (e.g., `GetSchedulesUseCase`).
- **Structure**:
  ```kotlin
  class GetSchedulesUseCase @Inject constructor(
      private val repository: ScheduleRepository
  ) {
      operator fun invoke(): Flow<List<Schedule>> = repository.getAllSchedules()
  }
  ```

### 2. Data Layer (Repository & Mappers)
- **Mappers**: ALWAYS map between `Entity` (Database/Network) and `Model` (Domain).
- **Pattern**: Use extension functions in `DataMapper` or similar objects.
- **Implementation**:
  ```kotlin
  // data/repository/ScheduleRepositoryImpl.kt
  override fun getAllSchedules(): Flow<List<Schedule>> {
      return scheduleDao.getAllSchedules().map { list ->
          list.map { it.toDomain() } // Mapping happens here
      }
  }
  ```

### 3. Feature Layer (MVVM)
- **ViewModel**: Use `StateFlow` for UI state. Inject UseCases.
- **UIState**: Expose a single state object or multiple flows depending on complexity.
- **Code Style**:
  ```kotlin
  @HiltViewModel
  class ScheduleViewModel @Inject constructor(
      private val getSchedulesUseCase: GetSchedulesUseCase
  ) : ViewModel() {
      // Logic using UseCase
  }
  ```

### 4. UI (Compose)
- **Navigation**: Managed in `app/ui/MainEntry.kt` using `androidx.navigation`.
- **Naming**: PascalCase for Composables (`PlanScreen`, `BottomNavigationBar`).
- **Previews**: Use `@Preview` for UI components.

## Common Workflows

### Build & Test
- **Build Debug APK**: `./gradlew assembleDebug`
- **Run Unit Tests**: `./gradlew test` (Runs tests in all modules).
- **Dependency check**: Check `gradle/libs.versions.toml` for version management.

### Adding a New Feature
1. Create module in `feature/<name>`.
2. Add to `settings.gradle.kts` (`include(":feature:<name>")`).
3. Add dependencies in `feature/<name>/build.gradle.kts`.
4. Define Domain models and UseCases in `domain`.
5. Implement Repository in `data`.
6. Implement UI and ViewModel in `feature/<name>`.
7. Register navigation in `app/ui/MainEntry.kt`.

## Conventions
- **Comments**: Code comments are in **Chinese**. Maintain this when updating existing documented methods.
- **Imports**: Use `javax.inject.Inject` for standard injection.
- **String Routes**: Current navigation uses string routes (e.g., "start", "home").
