package com.example.businesscard.connect

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.R
import com.example.businesscard.ble.BleServices
import com.example.businesscard.supabase
import com.example.businesscard.supabase.ConnectResult
import com.example.businesscard.supabase.User
import com.example.businesscard.supabase.UserRepository
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
    val networkMode: Boolean,
    val users: List<User>,
    val userMessage: Int? = null
)

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bleServices: BleServices
) : ViewModel() {
    private val _networkMode = MutableStateFlow<Boolean>(false)
    private val _userMessage = MutableStateFlow<Int?>(null)

    private val _users = bleServices.userIds.map { ids ->
        userRepository.getUsers(ids)
    }

    val uiState: StateFlow<ConnectUiState> = combine(_networkMode, _users, _userMessage) { networkMode, users, userMessage ->
        ConnectUiState(networkMode, users, userMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectUiState(false, emptyList(), null))

    var stopScanJob: Job? = null

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun advertise() {
        _networkMode.value = !_networkMode.value
        if(_networkMode.value) {
            viewModelScope.launch {
                bleServices.startAdvertising(supabase.auth.currentUserOrNull()!!.id.toByteArray())
            }
        } else {
            viewModelScope.launch {
                bleServices.stopAdvertising()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun refreshScan() {
        if(_networkMode.value) {
            viewModelScope.launch {
                bleServices.startScanning()
            }
            viewModelScope.launch {
                stopScanJob?.cancelAndJoin()
                delay(10000)
                bleServices.stopScanning()
                stopScanJob = null
            }
        } else {
            viewModelScope.launch {
                stopScanJob?.cancelAndJoin()
                bleServices.stopScanning()
                stopScanJob = null
            }
        }
    }

    fun connectWith(user: User) {
        viewModelScope.launch {
            val result = userRepository.requestConnection(user.id)
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