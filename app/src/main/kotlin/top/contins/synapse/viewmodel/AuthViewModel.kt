package top.contins.synapse.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import top.contins.synapse.service.AuthService
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    fun login(
        serverEndpoint: String,
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
    }

    fun register(
        serverEndpoint: String,
        email: String,
        password: String,
        captchaId: String,
        captchaCode: String,
        callback: (Boolean, String?) -> Unit
    ) {
    }

    fun getCaptcha(
        serverEndpoint: String,
    ) {

    }

    fun afterLogin(
        serverEndpoint: String,
        email: String,
        password: String,
    ) {

    }
}
