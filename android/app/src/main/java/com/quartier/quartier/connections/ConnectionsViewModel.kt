package com.quartier.quartier.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.R
import com.quartier.quartier.database.AuthRepository
import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.ConnectionsDatabase
import com.quartier.quartier.database.ConnectionsRepository
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsDatabase
import com.quartier.quartier.database.SocialsRepository
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserDatabase
import com.quartier.quartier.database.UserRepository
import com.quartier.quartier.supabase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//The viewmodel for the Connections screen, handles connections fetching and status modification
data class ConnectionsUIState(
    val requests: List<User>,
    val connections: List<User>,
    val connectionsSocials: Map<String, Socials>,
    val isRefreshing: Boolean,
    val userMessage: Int?
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val socialsRepository: SocialsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _requests: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _connections: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _connectionsSocials: MutableStateFlow<Map<String, Socials>> = MutableStateFlow(emptyMap())

    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ConnectionsUIState> = combine(_requests, _connections, _connectionsSocials, _isRefreshing, _userMessage) { requests, connections, connectionsSocials, isRefreshing, userMessage ->
        ConnectionsUIState(requests, connections, connectionsSocials, isRefreshing, userMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionsUIState(emptyList(), emptyList(), emptyMap(),false, null))

    init {
        refreshConnections()
    }

    fun refreshConnections() {
        _isRefreshing.value = true
        viewModelScope.launch {
            val connections = connectionsRepository.getConnections()

            val connectedUsers = connectionsToUsers(connections)

            _requests.value = connectedUsers.filter { user -> user.connectionStatus == "pending" }
            _connections.value = connectedUsers.filter { user -> user.connectionStatus == "accepted" }
            _isRefreshing.value = false
        }
        viewModelScope.launch {
            val socials = socialsRepository.getUserSocialsList()
            _connectionsSocials.value = socials.associateBy { it.id }
        }
    }

    suspend fun connectionsToUsers(connections: List<Connection>) : List<User> {
        val uid = authRepository.userId.value!!
        val connectionsMap = connections.associateBy(
            {if (it.requested_by == uid) it.requested_for else it.requested_by}, //find the other user's id
            {it.status}
        )
        val users = userRepository.getUsers(connectionsMap.keys.toList())
        return users.map { user ->
            User(user.id, user.name, user.job, user.pfp_url, connectionsMap[user.id])
        }
    }

    fun acceptConnection(user: User) {
        viewModelScope.launch {
            connectionsRepository.acceptConnection(user.id)
            refreshConnections()
        }
    }

    fun declineConnection(user: User) {
        viewModelScope.launch {
            connectionsRepository.deleteConnection(user.id)
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