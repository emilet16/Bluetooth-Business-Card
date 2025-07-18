package com.quartier.quartier.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(val authManager: AuthManager) : ViewModel() {
    private val _userMessage = MutableStateFlow<Int?>(null)
    val uiState = _userMessage.asStateFlow()

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    fun emailSignUp(name: String, email: String, pwd: String) = viewModelScope.launch {
        val result = authManager.emailSignUp(name, email, pwd)
        if(result is AuthResult.Error) {
            _userMessage.value = messageFromErrorCode(result.error)
        }
    }

    fun matchesEmailRegex(input: String): Boolean {
        val emailRegex = Regex("^[^@]+@[^@]+\\.[^@]+\$")
        return emailRegex.matches(input)
    }
}