package com.quartier.quartier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _sessionStatus: MutableStateFlow<SessionStatus> = MutableStateFlow(SessionStatus.Initializing)

    private var shouldRefreshMainScreen = true //prevent the screen from changing when tabbed out

    val sessionStatus = _sessionStatus.asStateFlow()

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect {
                when(it) {
                    is SessionStatus.Authenticated -> {
                        when(it.source) {
                            is SessionSource.SignIn -> {_sessionStatus.value = it}
                            is SessionSource.SignUp -> {_sessionStatus.value = it}
                            is SessionSource.Storage -> {
                                if(shouldRefreshMainScreen) { //If the user was already logged in, ignore
                                    _sessionStatus.value = it
                                    shouldRefreshMainScreen = false
                                }
                            }
                            else -> {} //ignore unwanted events
                        }
                    }
                    is SessionStatus.Initializing -> {
                        if(shouldRefreshMainScreen) { //If the user was already logged in, ignore
                            _sessionStatus.value = it
                        }
                    }
                    else -> {
                        _sessionStatus.value = it
                    }
                }
            }
        }
    }
}

