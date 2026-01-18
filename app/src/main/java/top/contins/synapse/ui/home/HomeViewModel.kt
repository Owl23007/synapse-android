package top.contins.synapse.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.contins.synapse.domain.repository.TokenRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _isGuest = MutableStateFlow(false)
    val isGuest = _isGuest.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        // Simple check: if no valid tokens, treat as guest
        _isGuest.value = !tokenRepository.hasValidTokens()
    }
    
    fun refreshUserStatus() {
        checkUserStatus()
    }
}
