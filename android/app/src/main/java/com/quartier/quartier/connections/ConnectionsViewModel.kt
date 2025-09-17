package com.quartier.quartier.connections

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.BleRepository
import com.quartier.quartier.R
import com.quartier.quartier.database.AuthRepository
import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.ConnectionRequestResult
import com.quartier.quartier.database.ConnectionsRepository
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsRepository
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

data class NearbyUsersState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val users: List<User>,
    val show: Boolean
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val socialsRepository: SocialsRepository,
    private val authRepository: AuthRepository,
    private val bleRepository: BleRepository
) : ViewModel() {
    private val _requests: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _connections: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _connectionsSocials: MutableStateFlow<Map<String, Socials>> = MutableStateFlow(emptyMap())

    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ConnectionsUIState> = combine(_requests, _connections,
        _connectionsSocials, _isRefreshing,  _userMessage) { requests, connections, connectionsSocials, isRefreshing, userMessage ->
        ConnectionsUIState(requests, connections, connectionsSocials, isRefreshing, userMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionsUIState(emptyList(), emptyList(), emptyMap(),false, null))

    private val _users = bleRepository.userIds.map { ids ->
        userRepository.getUsers(ids)
    }
    private val _showNearbyUsers = MutableStateFlow(false)

    val nearbyUsersState: StateFlow<NearbyUsersState> = combine(_users, _showNearbyUsers) { users, show ->
        NearbyUsersState(users, show)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NearbyUsersState(emptyList(), false))

    private var stopScanJob: Job? = null

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

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN])
    fun openNearbyUsers() {
        showNearbyUsers()
        viewModelScope.launch {
            bleRepository.startAdvertising()
        }
        viewModelScope.launch {
            bleRepository.startScanning()
        }
        //Scan for only 10 seconds to optimize resource usage
        viewModelScope.launch {
            stopScanJob?.cancelAndJoin()
            delay(10000)
            bleRepository.stopScanning()
            stopScanJob = null
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN])
    fun closeNearbyUsers() {
        hideNearbyUsers()
        viewModelScope.launch {
            bleRepository.stopAdvertising()
        }
        viewModelScope.launch {
            bleRepository.stopScanning()
            stopScanJob?.cancelAndJoin()
            stopScanJob = null
        }
        viewModelScope.launch {
            refreshConnections()
        }
    }

    fun connectWith(user: User) {
        viewModelScope.launch {
            val connection = connectionsRepository.getConnectionWithUser(requestedId = user.id)
            if(connection == null) {
                //No connection was found, check if the user tried connecting with themselves
                val result = connectionsRepository.requestConnection(requestedId = user.id)
                if(result == ConnectionRequestResult.Success)  _userMessage.value = R.string.connection_request_success
                else if(result == ConnectionRequestResult.CannotConnectWithSelf) _userMessage.value = R.string.error_request_self
            } else if(connection.status == "pending" && connection.requested_by == user.id) {
                //A connection has been sent by this user, accept it.
                connectionsRepository.acceptConnection(user2Id = user.id)
                _userMessage.value = R.string.connection_request_accepted
            } else if(connection.status == "accepted") {
                //The users are already connected
                _userMessage.value = R.string.already_connected
            } else if(connection.status == "pending" && connection.requested_for == user.id) {
                //The connection request has been sent, wait for the other to accept.
                _userMessage.value = R.string.connection_request_wait
            } else {
                //Notify the user if something unexpected happens
                _userMessage.value = R.string.unexpected_error
            }
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

    private fun showNearbyUsers() {
        _showNearbyUsers.value = true
    }

    private fun hideNearbyUsers() {
        _showNearbyUsers.value = false
    }

    fun bluetoothPermissionsDeclined() {
        _userMessage.value = R.string.bluetooth_permission_denied
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }
}