package com.example.businesscard.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(val authManager: AuthManager) : ViewModel() {
    private val _userMessage = MutableStateFlow<Int?>(null)
    val uiState = _userMessage.asStateFlow()

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    fun emailSignIn(email: String, pwd: String) = viewModelScope.launch {
        val result = authManager.emailSignIn(email, pwd)
        if(result is AuthResult.Error) {
            _userMessage.value = messageFromErrorCode(result.error)
        }
    }
}