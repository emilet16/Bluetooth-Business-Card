package com.example.businesscard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class NavigationState(
    val sessionStatus: SessionStatus,
    val savedScreen: Screen
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _sessionStatus: MutableStateFlow<SessionStatus> = MutableStateFlow(SessionStatus.Initializing)
    private val _savedScreen: MutableStateFlow<Screen> = MutableStateFlow(Screen.Connections)

    val navigationState: StateFlow<NavigationState> = combine(_sessionStatus, _savedScreen) { status, screen ->
        NavigationState(status, screen)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NavigationState(SessionStatus.Initializing, Screen.Connections))

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect {
                _sessionStatus.value = it
            }
        }
    }

    fun navigate(to: Screen) {
        _savedScreen.value = to
    }
}

