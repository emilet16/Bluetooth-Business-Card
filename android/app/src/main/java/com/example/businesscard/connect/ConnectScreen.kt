package com.example.businesscard.connect

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.businesscard.R
import com.example.businesscard.components.MainBottomAppBar
import com.example.businesscard.components.SelectedScreen
import com.example.businesscard.components.UserCard
import com.example.businesscard.supabase.User

@Composable
fun ConnectScreen(viewModel: ConnectViewModel = hiltViewModel(), onNavToConnections: ()->Unit, onNavToProfile: ()->Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }, bottomBar = {
        MainBottomAppBar(onNavToProfile = onNavToProfile, onNavToConnections = onNavToConnections, selectedScreen = SelectedScreen.Home)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ConnectScreen(uiState.networkMode, uiState.users, onBleDisabled = {viewModel.bleDisabled()}, onAdvertise = {viewModel.advertise()},
                onScan = {viewModel.refreshScan()}, onPermissionDenied = {viewModel.bluetoothPermissionDenied()}, onSendProfileRequest = { viewModel.connectWith(it) })

            uiState.userMessage?.let { userMessage ->
                val snackbarText = LocalContext.current.getString(userMessage)
                LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                    viewModel.snackbarMessageShown()
                }
            }
        }
    }
}

@Composable
private fun ConnectScreen(networkMode: Boolean, users: List<User>, onBleDisabled: ()->Unit, onAdvertise: ()->Unit, onScan: ()->Unit, onPermissionDenied: () -> Unit, onSendProfileRequest: (User)->Unit) {
    var advAllowed = ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    var scanAllowed = ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

    val bluetoothManager = LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bleAdapter = bluetoothManager.adapter

    val requestScan = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        scanAllowed = granted
        if(granted) {
            onScan()
        } else {
            onPermissionDenied()
        }
    }

    val requestAdv = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        advAllowed = granted
        if(granted) {
            advAllowed = true
            onAdvertise()
            if(scanAllowed) {
                onScan()
            } else {
                requestScan.launch(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            onPermissionDenied()
        }
    }

    Column {
        Card{
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Network mode")
                Spacer(modifier = Modifier.size(5.dp))
                Switch(checked = networkMode, onCheckedChange = {
                    if(bleAdapter.isEnabled) {
                        if(advAllowed) {
                            onAdvertise()
                            if(scanAllowed) {
                                onScan()
                            } else {
                                requestScan.launch(Manifest.permission.BLUETOOTH_SCAN)
                            }
                        } else {
                            requestAdv.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
                        }
                    } else {
                        onBleDisabled()
                    }
                })
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)) {
                    Text(LocalContext.current.getString(R.string.nearby_people_label))
                    IconButton(onClick = {
                        if(bleAdapter.isEnabled) {
                            if(scanAllowed) {
                                onScan()
                            } else {
                                requestScan.launch(Manifest.permission.BLUETOOTH_SCAN)
                            }
                        } else {
                            onBleDisabled()
                        }
                    }, enabled = networkMode) {
                        Icon(Icons.Default.Refresh, LocalContext.current.getString(R.string.refresh_desc))
                    }
                }
                LazyVerticalGrid(columns = GridCells.FixedSize(200.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (user in users) {
                        item {
                            UserCard(onClick = {onSendProfileRequest(user)}, user)
                        }
                    }
                }
            }
        }
    }
}