package com.example.businesscard.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.R
import com.example.businesscard.Socials
import com.example.businesscard.User
import com.example.businesscard.UserRepository
import com.example.businesscard.supabase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionsUIState(
    val requests: List<User>,
    val connections: List<User>,
    val connectionsSocials: Map<String, Socials>,
    val userMessage: Int?
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {
    private val _requests: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _connections: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _connectionsSocials: MutableStateFlow<Map<String, Socials>> = MutableStateFlow(emptyMap())
    private val _userMessage = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ConnectionsUIState> = combine(_requests, _connections, _connectionsSocials, _userMessage) { requests, connections, connectionsSocials, userMessage ->
        ConnectionsUIState(requests, connections, connectionsSocials, userMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionsUIState(emptyList(), emptyList(), emptyMap(), null))

    private val userId: String = supabase.auth.currentUserOrNull()!!.id

    init {
        refreshConnections()
    }

    private fun refreshConnections() {
        viewModelScope.launch {
            val connectedUsers = userRepository.getConnectedUsers(userId)

            _requests.value = connectedUsers.filter { user -> user.connectionStatus == "pending" }
            _connections.value = connectedUsers.filter { user -> user.connectionStatus == "accepted" }
        }
        viewModelScope.launch {
            val socials = userRepository.getUserSocialsList()
            _connectionsSocials.value = socials.associateBy { it.id }
        }
    }

    fun acceptConnection(user: User) {
        viewModelScope.launch {
            userRepository.acceptConnection(userId, user.id)
            refreshConnections()
        }
    }

    fun declineConnection(user: User) {
        viewModelScope.launch {
            userRepository.deleteConnection(userId, user.id)
            refreshConnections()
        }
    }

    fun bluetoothPermissionsDeclined() {
        _userMessage.value = R.string.bluetooth_permission_denied
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }
}