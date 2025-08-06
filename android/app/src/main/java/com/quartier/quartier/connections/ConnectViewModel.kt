package com.quartier.quartier.connections

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.BleRepository
import com.quartier.quartier.R
import com.quartier.quartier.database.ConnectionRequestResult
import com.quartier.quartier.database.ConnectionsRepository
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

//Viewmodel for the Connect screen, fetching nearby users, their profiles and handling requests

data class ConnectUiState(
    val users: List<User>,
    val userMessage: Int? = null
)

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val bleRepository: BleRepository
) : ViewModel(), DefaultLifecycleObserver {
    private val _userMessage = MutableStateFlow<Int?>(null)

    private val _users = bleRepository.userIds.map { ids ->
        userRepository.getUsers(ids)
    }

    val uiState: StateFlow<ConnectUiState> = combine(_users, _userMessage) { users, userMessage ->
        ConnectUiState(users, userMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectUiState(emptyList(), null))

    private var stopScanJob: Job? = null

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        viewModelScope.launch {
            bleRepository.startAdvertising()
        }
        viewModelScope.launch {
            refreshScan()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        viewModelScope.launch {
            bleRepository.stopAdvertising()
        }
        viewModelScope.launch {
            bleRepository.stopScanning()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun refreshScan() {
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

    fun bleDisabled() {
        _userMessage.value = R.string.bluetooth_disabled
    }
}