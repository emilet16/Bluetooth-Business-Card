package com.example.businesscard.connect

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.businesscard.R
import com.example.businesscard.components.UserCard
import com.example.businesscard.supabase.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(viewModel: ConnectViewModel = hiltViewModel(), onExit: ()->Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    viewModel.observeLifecycle(LocalLifecycleOwner.current.lifecycle)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bluetoothManager = LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bleAdapter = bluetoothManager.adapter

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onExit) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalContext.current.getString(R.string.back))
                }
            },
            title = {
                Text("Nearby people")
            }, actions = {
                IconButton(onClick = {
                    if(bleAdapter.isEnabled) {
                        viewModel.refreshScan()
                    } else {
                        viewModel.bleDisabled()
                    }
                }) {
                    Icon(Icons.Default.Refresh, LocalContext.current.getString(R.string.refresh_desc))
                }
            }
        )
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ConnectScreen(uiState.users, onSendProfileRequest = { viewModel.connectWith(it) })

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
private fun ConnectScreen(users: List<User>, onSendProfileRequest: (User)->Unit) {
    LazyVerticalGrid(columns = GridCells.FixedSize(200.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (user in users) {
            item {
                UserCard(onClick = {onSendProfileRequest(user)}, user)
            }
        }
    }
}

@Composable
fun <LO : LifecycleObserver> LO.observeLifecycle(lifecycle: Lifecycle) {
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(this@observeLifecycle)
        onDispose {
            lifecycle.removeObserver(this@observeLifecycle)
        }
    }
}