package com.quartier.quartier.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.R
import com.quartier.quartier.database.ConnectionsDatabase
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsDatabase
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserDatabase
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

data class ConnectionsUIState(
    val requests: List<User>,
    val connections: List<User>,
    val connectionsSocials: Map<String, Socials>,
    val isRefreshing: Boolean,
    val userMessage: Int?
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(private val userDatabase: UserDatabase,
                                               private val connectionsDatabase: ConnectionsDatabase,
                                               private val socialsDatabase: SocialsDatabase) : ViewModel() {
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
            val connections = connectionsDatabase.getConnections()
            val connectedUsers = userDatabase.getConnectedUsers(connections)

            _requests.value = connectedUsers.filter { user -> user.connectionStatus == "pending" }
            _connections.value = connectedUsers.filter { user -> user.connectionStatus == "accepted" }
            _isRefreshing.value = false
        }
        viewModelScope.launch {
            val socials = socialsDatabase.getUserSocialsList()
            _connectionsSocials.value = socials.associateBy { it.id }
        }
    }

    fun acceptConnection(user: User) {
        viewModelScope.launch {
            connectionsDatabase.acceptConnection(user.id)
            refreshConnections()
        }
    }

    fun declineConnection(user: User) {
        viewModelScope.launch {
            connectionsDatabase.deleteConnection(user.id)
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