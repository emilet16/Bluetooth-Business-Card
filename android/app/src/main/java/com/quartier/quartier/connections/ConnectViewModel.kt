package com.quartier.quartier.connections

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.R
import com.quartier.quartier.BleServices
import com.quartier.quartier.supabase
import com.quartier.quartier.database.ConnectResult
import com.quartier.quartier.database.ConnectionsDatabase
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
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

data class ConnectUiState(
    val users: List<User>,
    val userMessage: Int? = null
)

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val userDatabase: UserDatabase,
    private val connectionsDatabase: ConnectionsDatabase,
    private val bleServices: BleServices
) : ViewModel(), DefaultLifecycleObserver {
    private val _userMessage = MutableStateFlow<Int?>(null)

    private val _users = bleServices.userIds.map { ids ->
        userDatabase.getUsers(ids)
    }

    private val userID = supabase.auth.currentUserOrNull()!!.id

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
            bleServices.startAdvertising(userID)
        }
        viewModelScope.launch {
            refreshScan()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        viewModelScope.launch {
            bleServices.stopAdvertising()
        }
        viewModelScope.launch {
            bleServices.stopScanning()
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun refreshScan() {
        viewModelScope.launch {
            bleServices.startScanning()
        }
        viewModelScope.launch {
            stopScanJob?.cancelAndJoin()
            delay(10000)
            bleServices.stopScanning()
            stopScanJob = null
        }
    }

    fun connectWith(user: User) {
        viewModelScope.launch {
            val result = connectionsDatabase.requestConnection(user.id)
            when(result) {
                ConnectResult.Pending -> {
                    _userMessage.value = R.string.connection_request_wait
                }
                ConnectResult.Requested -> {
                    _userMessage.value = R.string.connection_request_success
                }
                ConnectResult.Accepted -> {
                    _userMessage.value = R.string.connection_request_accepted
                }
                ConnectResult.AlreadyConnected -> {
                    _userMessage.value = R.string.already_connected
                }
                is ConnectResult.Error -> {
                    _userMessage.value = R.string.unexpected_error //TODO: error handling?
                }
            }
        }
    }

    fun bluetoothPermissionDenied() {
        _userMessage.value = R.string.bluetooth_permission_denied
    }

    fun bleDisabled() {
        _userMessage.value = R.string.bluetooth_disabled
    }
}