package com.quartier.quartier.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsDatabase
import com.quartier.quartier.database.SocialsRepository
import com.quartier.quartier.supabase
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserDatabase
import com.quartier.quartier.database.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileScreenState(
    val user: User?,
    val socials: Socials?,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val socialsRepository: SocialsRepository
) : ViewModel() {
    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _socials: MutableStateFlow<Socials?> = MutableStateFlow(null)
    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<ProfileScreenState> = combine(_user, _socials, _isRefreshing) { user, socials, isRefreshing ->
        ProfileScreenState(user, socials, isRefreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileScreenState(null, null, false))

    init {
        refreshUser()
    }

    fun refreshUser() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _user.value = userRepository.getUser()
            _isRefreshing.value = false
        }
        viewModelScope.launch {
            _socials.value = socialsRepository.getUserSocials()
        }
    }
}