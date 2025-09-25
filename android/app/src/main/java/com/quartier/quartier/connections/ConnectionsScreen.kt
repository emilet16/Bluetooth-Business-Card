package com.quartier.quartier.connections

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quartier.quartier.R
import com.quartier.quartier.components.ConnectionRequest
import com.quartier.quartier.components.MainBottomAppBar
import com.quartier.quartier.components.Placeholder
import com.quartier.quartier.components.SelectedScreen
import com.quartier.quartier.components.UserCard
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.User
import com.quartier.quartier.ui.theme.Typography

//A screen display all connections and requests for the user

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(viewModel: ConnectionsViewModel = hiltViewModel(), onNavToProfile: ()->Unit, onNavToLinkedin: (String) -> Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val nearbyUsersState by viewModel.nearbyUsersState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val requestScanPerm = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if(granted) {
            viewModel.openNearbyUsers()
        } else {
            viewModel.bluetoothPermissionsDeclined()
        }
    }

    val requestAdvPerm = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if(granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                viewModel.openNearbyUsers()
            } else {
                requestScanPerm.launch(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            viewModel.bluetoothPermissionsDeclined()
        }
    }

    val bluetoothManager = LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bleAdapter = bluetoothManager.adapter

    Scaffold(modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            MainBottomAppBar(onNavToProfile = onNavToProfile, selectedScreen = SelectedScreen.Connections)
        }, floatingActionButton = {
            Button(onClick = {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val advAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
                    val scanAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    if(advAllowed && scanAllowed && bleAdapter.isEnabled) {
                        viewModel.openNearbyUsers()
                    } else {
                        if(!advAllowed) {
                            requestAdvPerm.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
                        }
                        if(!scanAllowed) {
                            requestScanPerm.launch(Manifest.permission.BLUETOOTH_SCAN)
                        }
                        if(!bleAdapter.isEnabled) {
                            viewModel.bluetoothDisabled()
                        }
                    }
                } else {
                    viewModel.openNearbyUsers()
                }
            }) {
                Icon(Icons.Outlined.Add, LocalContext.current.getString(R.string.add_connections))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ConnectionsScreen(uiState.requests, uiState.connections, uiState.connectionsSocials, onAcceptConnection = {viewModel.acceptConnection(it)},
                onDeclineConnection = {viewModel.declineConnection(it)}, onNavToLinkedin = onNavToLinkedin, isRefreshing = uiState.isRefreshing, onRefresh = {
                    viewModel.refreshConnections()
                }, onOpenNearbyConnections = {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val advAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
                        val scanAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                        if(advAllowed && scanAllowed && bleAdapter.isEnabled) {
                            viewModel.openNearbyUsers()
                        } else {
                            if(!advAllowed) {
                                requestAdvPerm.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
                            }
                            if(!scanAllowed) {
                                requestScanPerm.launch(Manifest.permission.BLUETOOTH_SCAN)
                            }
                            if(!bleAdapter.isEnabled) {
                                viewModel.bluetoothDisabled()
                            }
                        }
                    } else {
                        viewModel.openNearbyUsers()
                    }
                })
        }
    }

    if(nearbyUsersState.show) {
        NearbyUsers(users = nearbyUsersState.users, onSendProfileRequest = {
            viewModel.connectWith(it)
        }, onDismiss = {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val advAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
                val scanAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                if(advAllowed && scanAllowed && bleAdapter.isEnabled) {
                    viewModel.closeNearbyUsers()
                }
            } else {
                viewModel.closeNearbyUsers()
            }
        }, snackbarHostState = snackbarHostState)
    }

    //Display message for the user (ex. when Bluetooth isn't available)
    uiState.userMessage?.let { userMessage ->
        val snackbarText = LocalContext.current.getString(userMessage)
        LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
            snackbarHostState.showSnackbar(snackbarText)
            viewModel.snackbarMessageShown()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionsScreen(
    requests: List<User>, connections: List<User>, socials: Map<String, Socials>,
    onAcceptConnection: (User)->Unit, onDeclineConnection: (User)->Unit, onNavToLinkedin: (String)->Unit,
    isRefreshing: Boolean, onRefresh: ()->Unit, onOpenNearbyConnections: ()->Unit
) {
    var showRequests by rememberSaveable { mutableStateOf(true) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable(onClick = {
                            showRequests = !showRequests
                        }),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Connection requests",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        showRequests = !showRequests
                    }) {
                        val icon = if (showRequests) {
                            Icons.Outlined.KeyboardArrowUp
                        } else {
                            Icons.Outlined.KeyboardArrowDown
                        }

                        val iconDesc = if (showRequests) {
                            R.string.hide_requests
                        } else {
                            R.string.show_requests
                        }

                        Icon(
                            icon,
                            LocalContext.current.getString(iconDesc)
                        )
                    }
                }
            }

            if (showRequests) {
                if (requests.isEmpty()) {
                    item {
                        Placeholder(
                            title = LocalContext.current.getString(R.string.requests_empty),
                            body = LocalContext.current.getString(R.string.encouragement_networking),
                            icon = painterResource(R.drawable.outline_business_center_24)
                        )
                    }
                } else {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (request in requests) {
                                item {
                                    ConnectionRequest(
                                        onAcceptConnection = onAcceptConnection,
                                        onDeclineConnection = onDeclineConnection,
                                        user = request
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }

            item {
                Text("Connections", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (connections.isEmpty()) {
                item {
                    Placeholder(
                        title = LocalContext.current.getString(R.string.grow_network),
                        body = LocalContext.current.getString(R.string.connect_nearby_users),
                        icon = Icons.Outlined.Person,
                        buttonText = LocalContext.current.getString(R.string.add_connections),
                        buttonIcon = Icons.Outlined.Add,
                        action = onOpenNearbyConnections
                    )
                }
            } else {
                for (connection in connections) {
                    item {
                        UserCard(onClick = {
                            val url = socials[connection.id]?.linkedin_url
                            if (url != null) onNavToLinkedin(url)
                        }, user = connection)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnectionsScreen() {
    ConnectionsScreen(requests = listOf(User("0", "Steve Jobs", "CEO")),
        connections = listOf(User("1", "Bill Gates", "Philanthropist")),
        socials = mapOf(),
        onAcceptConnection = {_ ->}, onDeclineConnection = {_ -> }, onNavToLinkedin = {_ ->},
        isRefreshing = false, onRefresh = {}, onOpenNearbyConnections = {})
}