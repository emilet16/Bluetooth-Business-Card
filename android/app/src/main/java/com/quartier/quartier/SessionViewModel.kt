package com.quartier.quartier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.database.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//Listens to the supabase auth status and inform the navigation of the status
//Stores the user id top prevent weird behaviors when supabase checks the userId on app refresh

@HiltViewModel
class SessionViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect {
                if(it is SessionStatus.Authenticated) {
                    _isLoggedIn.value = true
                    authRepository.updateUserId(it.session.user!!.id)
                } else if(it is SessionStatus.NotAuthenticated || it is SessionStatus.RefreshFailure) {
                    _isLoggedIn.value = false
                }
            }
        }
    }
}

