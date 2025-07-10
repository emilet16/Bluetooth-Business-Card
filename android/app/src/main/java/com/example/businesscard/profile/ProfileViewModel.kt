package com.example.businesscard.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.Socials
import com.example.businesscard.supabase
import com.example.businesscard.User
import com.example.businesscard.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileScreenState(
    val user: User?,
    val socials: Socials?
)

@HiltViewModel
class ProfileViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _socials: MutableStateFlow<Socials?> = MutableStateFlow(null)

    val uiState: StateFlow<ProfileScreenState> = combine(_user, _socials) { user, socials ->
        ProfileScreenState(user, socials)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileScreenState(null, null))

    private val userId: String = supabase.auth.currentUserOrNull()!!.id

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
                _user.value = userRepository.getUser(userId)
            }
            viewModelScope.launch {
                _socials.value = userRepository.getUserSocials(supabase.auth.currentUserOrNull()!!.id)
            }
        }
    }
}