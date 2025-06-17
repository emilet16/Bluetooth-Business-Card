package com.example.businesscard.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.supabase
import com.example.businesscard.supabase.User
import com.example.businesscard.supabase.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {
    private val _userState: MutableStateFlow<User?> = MutableStateFlow(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect {
                refreshUser()
            }
        }
    }

    fun refreshUser() {
        if(supabase.auth.sessionStatus.value is SessionStatus.Authenticated) {
            viewModelScope.launch {
                _userState.value = userRepository.getCurrentUser()
            }
        }
    }
}