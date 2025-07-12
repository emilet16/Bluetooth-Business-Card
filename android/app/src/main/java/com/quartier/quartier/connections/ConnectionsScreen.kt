package com.quartier.quartier.connections

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quartier.quartier.R
import com.quartier.quartier.components.MainBottomAppBar
import com.quartier.quartier.components.SelectedScreen
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.User
import com.quartier.quartier.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(viewModel: ConnectionsViewModel = hiltViewModel(), onNavToConnect: ()->Unit, onNavToProfile: ()->Unit, onNavToLinkedin: (String) -> Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val advAllowed = ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    val scanAllowed = ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

    val requestScanPerm = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if(granted) {
            onNavToConnect()
        } else {
            viewModel.bluetoothPermissionsDeclined()
        }
    }

    val requestAdvPerm = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if(granted) {
            if(scanAllowed) {
                onNavToConnect()
            }
            else {
                requestScanPerm.launch(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        else {
            viewModel.bluetoothPermissionsDeclined()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(title = {
            Text(LocalContext.current.getString(R.string.connections_title))
        }, actions = {
            IconButton(onClick = {
                if(advAllowed && scanAllowed) {
                    onNavToConnect()
                } else if(!advAllowed) {
                    requestAdvPerm.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
                }
                else if(!scanAllowed) {
                    requestScanPerm.launch(Manifest.permission.BLUETOOTH_SCAN)
                }
            }) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = LocalContext.current.getString(R.string.add_connections),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        })
    }, bottomBar = {
        MainBottomAppBar(onNavToProfile = onNavToProfile, selectedScreen = SelectedScreen.Connections)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ConnectionsScreen(uiState.requests, uiState.connections, uiState.connectionsSocials, onAcceptConnection = {viewModel.acceptConnection(it)},
                onDeclineConnection = {viewModel.declineConnection(it)}, onNavToLinkedin = onNavToLinkedin, isRefreshing = uiState.isRefreshing, onRefresh = {
                    viewModel.refreshConnections()
                })

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionsScreen(requests: List<User>, connections: List<User>, socials: Map<String, Socials>, onAcceptConnection: (User)->Unit, onDeclineConnection: (User)->Unit, onNavToLinkedin: (String)->Unit,
isRefreshing: Boolean, onRefresh: ()->Unit) {
    PullToRefreshBox(isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Connection requests", fontWeight = FontWeight.Bold)
            }
            if (requests.isEmpty()) {
                item {
                    Text(
                        "You have no connection requests",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (request in requests) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .wrapContentHeight()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(5.dp)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(
                                                    request.pfp_url
                                                        ?: R.drawable.baseline_account_circle
                                                )
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = LocalContext.current.getString(R.string.pfp_description),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .border(2.dp, Color.Black, CircleShape)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                        )
                                        Text(
                                            request.name,
                                            style = Typography.titleSmall,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(request.job, textAlign = TextAlign.Center)
                                        Row(horizontalArrangement = Arrangement.SpaceAround) {
                                            IconButton({ onAcceptConnection(request) }) {
                                                Icon(
                                                    Icons.Default.Done,
                                                    contentDescription = LocalContext.current.getString(
                                                        R.string.accept_request
                                                    )
                                                )
                                            }
                                            IconButton({ onDeclineConnection(request) }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = LocalContext.current.getString(
                                                        R.string.decline_request
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Connections", fontWeight = FontWeight.Bold)
            }

            if (connections.isEmpty()) {
                item {
                    Text("You have no connections", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                for (connection in connections) {
                    item {
                        Card(onClick = {
                            val url = socials[connection.id]?.linkedin_url
                            if (url != null) onNavToLinkedin(url)
                        }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(
                                            connection.pfp_url ?: R.drawable.baseline_account_circle
                                        )
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = LocalContext.current.getString(R.string.pfp_description),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(150.dp)
                                        .border(2.dp, Color.Black, CircleShape)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(connection.name)
                                    Text(connection.job)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnectionsScreen() {
    ConnectionsScreen(requests = listOf<User>(User("0", "Steve Jobs", "CEO")),
        connections = listOf<User>(User("1", "Bill Gates", "Philantropist")),
        socials = mapOf<String, Socials>(),
        onAcceptConnection = {_ ->}, onDeclineConnection = {_ -> }, onNavToLinkedin = {_ ->},
        isRefreshing = false, onRefresh = {})
}