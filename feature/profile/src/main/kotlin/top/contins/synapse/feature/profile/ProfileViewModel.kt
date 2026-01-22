package top.contins.synapse.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.auth.AuthResult
import top.contins.synapse.domain.model.auth.User
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.usecase.auth.AuthUseCase
import top.contins.synapse.domain.usecase.auth.LogoutUseCase
import top.contins.synapse.domain.usecase.schedule.*
import top.contins.synapse.domain.usecase.subscription.*
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: User,
        val subscriptions: List<Subscription> = emptyList(),
        val recentSchedules: List<Schedule> = emptyList()
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object LoggingOut : ProfileUiState()
    object LoggedOut : ProfileUiState()
}

sealed class ScheduleManagementAction {
    object Idle : ScheduleManagementAction()
    data class ImportInProgress(val progress: String) : ScheduleManagementAction()
    data class ImportSuccess(val result: ImportResult) : ScheduleManagementAction()
    data class ImportError(val message: String) : ScheduleManagementAction()
    data class ExportSuccess(val icsContent: String) : ScheduleManagementAction()
    data class ExportError(val message: String) : ScheduleManagementAction()
    data class SyncInProgress(val subscriptionName: String) : ScheduleManagementAction()
    data class SyncSuccess(val subscriptionName: String, val result: SyncResult) : ScheduleManagementAction()
    data class SyncError(val message: String) : ScheduleManagementAction()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val authUseCase: AuthUseCase,
    private val getAllSubscriptionsUseCase: GetAllSubscriptionsUseCase,
    private val createSubscriptionUseCase: CreateSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val updateSubscriptionUseCase: UpdateSubscriptionUseCase,
    private val syncSubscriptionUseCase: SyncSubscriptionUseCase,
    private val importScheduleUseCase: ImportScheduleUseCase,
    private val exportScheduleUseCase: ExportScheduleUseCase,
    private val getSchedulesUseCase: GetSchedulesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _scheduleAction = MutableStateFlow<ScheduleManagementAction>(ScheduleManagementAction.Idle)
    val scheduleAction: StateFlow<ScheduleManagementAction> = _scheduleAction.asStateFlow()

    init {
        loadUserProfile()
        loadSubscriptions()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = authUseCase.checkAuth()) {
                is AuthResult.Success -> {
                    _uiState.value = ProfileUiState.Success(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }
    
    private fun loadSubscriptions() {
        viewModelScope.launch {
            getAllSubscriptionsUseCase()
                .catch { e ->
                    // Handle error silently, just use empty list
                }
                .collect { subscriptions ->
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        _uiState.value = currentState.copy(subscriptions = subscriptions)
                    }
                }
        }
    }

    /**
     * Import schedules from iCalendar content
     */
    fun importSchedules(
        icsContent: String,
        calendarId: String,
        conflictStrategy: ConflictStrategy = ConflictStrategy.SKIP
    ) {
        viewModelScope.launch {
            try {
                _scheduleAction.value = ScheduleManagementAction.ImportInProgress("正在导入日程...")
                
                val result = importScheduleUseCase(
                    icsContent = icsContent,
                    calendarId = calendarId,
                    handleConflicts = conflictStrategy
                )
                
                _scheduleAction.value = ScheduleManagementAction.ImportSuccess(result)
            } catch (e: Exception) {
                _scheduleAction.value = ScheduleManagementAction.ImportError(
                    e.message ?: "导入失败"
                )
            }
        }
    }
    
    /**
     * Export schedules to iCalendar format
     */
    fun exportSchedules(scheduleIds: List<String>) {
        viewModelScope.launch {
            try {
                val icsContent = exportScheduleUseCase(scheduleIds)
                _scheduleAction.value = ScheduleManagementAction.ExportSuccess(icsContent)
            } catch (e: Exception) {
                _scheduleAction.value = ScheduleManagementAction.ExportError(
                    e.message ?: "导出失败"
                )
            }
        }
    }
    
    /**
     * Create a new subscription
     */
    fun createSubscription(
        name: String,
        url: String,
        color: Long? = null,
        syncInterval: Int = 24
    ) {
        viewModelScope.launch {
            try {
                createSubscriptionUseCase(
                    name = name,
                    url = url,
                    color = color,
                    syncInterval = syncInterval
                )
            } catch (e: Exception) {
                _scheduleAction.value = ScheduleManagementAction.SyncError(
                    e.message ?: "创建订阅失败"
                )
            }
        }
    }
    
    /**
     * Delete a subscription
     */
    fun deleteSubscription(subscriptionId: String) {
        viewModelScope.launch {
            try {
                deleteSubscriptionUseCase(subscriptionId)
            } catch (e: Exception) {
                _scheduleAction.value = ScheduleManagementAction.SyncError(
                    e.message ?: "删除订阅失败"
                )
            }
        }
    }
    
    /**
     * Sync a subscription
     */
    fun syncSubscription(subscriptionId: String, subscriptionName: String) {
        viewModelScope.launch {
            try {
                _scheduleAction.value = ScheduleManagementAction.SyncInProgress(subscriptionName)
                
                val result = syncSubscriptionUseCase(subscriptionId)
                
                if (result.success) {
                    _scheduleAction.value = ScheduleManagementAction.SyncSuccess(
                        subscriptionName,
                        result
                    )
                } else {
                    _scheduleAction.value = ScheduleManagementAction.SyncError(
                        result.error ?: "同步失败"
                    )
                }
            } catch (e: Exception) {
                _scheduleAction.value = ScheduleManagementAction.SyncError(
                    e.message ?: "同步失败"
                )
            }
        }
    }
    
    /**
     * Reset schedule action state
     */
    fun resetScheduleAction() {
        _scheduleAction.value = ScheduleManagementAction.Idle
    }

    /**
     * Execute logout operation
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.LoggingOut
                
                // Execute logout logic
                logoutUseCase()
                
                // Notify UI to navigate to login page
                _uiState.value = ProfileUiState.LoggedOut
            } catch (e: Exception) {
                // Even if error occurs, should clear local data and navigate to login
                _uiState.value = ProfileUiState.LoggedOut
            }
        }
    }

    /**
     * Reset state (called after logout has been handled)
     */
    fun resetState() {
        _uiState.value = ProfileUiState.Loading
    }
}
