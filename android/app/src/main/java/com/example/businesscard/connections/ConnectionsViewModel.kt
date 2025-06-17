package com.example.businesscard.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.supabase.Socials
import com.example.businesscard.supabase.User
import com.example.businesscard.supabase.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {
    private val _requests: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    val requests = _requests.asStateFlow()

    private val _connections: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    val connections = _connections.asStateFlow()

    private val _connectionsSocials: MutableStateFlow<Map<String, Socials>> = MutableStateFlow(emptyMap())
    val connectionsSocials = _connectionsSocials.asStateFlow()

    init {
        refreshConnections()
    }

    fun refreshConnections() {
        viewModelScope.launch {
            val allConnections = userRepository.getConnectedUsers()
            _requests.value = allConnections["pending"]!!
            _connections.value = allConnections["accepted"]!!
            val socials = userRepository.getUserSocialsList(_connections.value.map { it.id })
            _connectionsSocials.value = socials.associateBy { it.id }
        }
    }

    fun acceptConnection(user: User) {
        viewModelScope.launch {
            userRepository.acceptConnection(user.id)
            refreshConnections()
        }
    }

    fun declineConnection(user: User) {
        viewModelScope.launch {
            userRepository.deleteConnection(user.id)
            refreshConnections()
        }
    }
}