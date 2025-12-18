package top.contins.synapse.feature.goal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.Goal
import top.contins.synapse.domain.repository.GoalRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = goalRepository.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createGoal(title: String, deadline: String) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = try {
                dateFormat.parse(deadline) ?: Date()
            } catch (e: Exception) {
                Date()
            }

            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                title = title,
                description = "",
                startDate = Date(),
                targetDate = targetDate,
                isCompleted = false,
                progress = 0
            )
            goalRepository.createGoal(newGoal)
        }
    }
}
