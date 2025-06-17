package com.example.businesscard.connections

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.businesscard.R
import com.example.businesscard.components.MainBottomAppBar
import com.example.businesscard.components.SelectedScreen
import com.example.businesscard.supabase.Socials
import com.example.businesscard.supabase.User
import com.example.businesscard.ui.theme.Typography

@Composable
fun ConnectionsScreen(viewModel: ConnectionsViewModel = hiltViewModel(), onNavToHome: ()->Unit, onNavToProfile: ()->Unit, onNavToLinkedin: (String) -> Unit) {
    val requests by viewModel.requests.collectAsStateWithLifecycle()
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val socials by viewModel.connectionsSocials.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
        MainBottomAppBar(onNavToHome = onNavToHome, onNavToProfile = onNavToProfile, selectedScreen = SelectedScreen.Connections)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ConnectionsScreen(requests, connections, socials, onAcceptConnection = {viewModel.acceptConnection(it)}, onDeclineConnection = {viewModel.declineConnection(it)}, onNavToLinkedin = onNavToLinkedin)
        }
    }
}

@Composable
private fun ConnectionsScreen(requests: List<User>, connections: List<User>, socials: Map<String, Socials>, onAcceptConnection: (User)->Unit, onDeclineConnection: (User)->Unit, onNavToLinkedin: (String)->Unit) {
    LazyColumn {
        item{
            Text("Connection requests")
        }

        item {
            if(requests.isEmpty()) {
                Text("You have no connection requests")
            } else {
                LazyRow {
                    for (request in requests) {
                        item {
                            Card( modifier = Modifier
                                .padding(5.dp)
                                .wrapContentHeight()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)) {
                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                                        .data(request.pfp_url ?: R.drawable.baseline_account_circle)
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
                                    Text(request.name, style = Typography.titleSmall, textAlign = TextAlign.Center)
                                    Text(request.job, textAlign = TextAlign.Center)
                                    Row(horizontalArrangement = Arrangement.SpaceAround) {
                                        IconButton({onAcceptConnection(request)}) {
                                            Icon(Icons.Default.Done, contentDescription = LocalContext.current.getString(R.string.accept_request))
                                        }
                                        IconButton({onDeclineConnection(request)}) {
                                            Icon(Icons.Default.Close, contentDescription = LocalContext.current.getString(R.string.decline_request))
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
            Text("Connections")
        }

        if(connections.isEmpty()) {
            item {
                Text("You have no connections")
            }
        } else {
            for(connection in connections) {
                item {
                    Card(onClick = {
                        val url = socials[connection.id]?.linkedin_url
                        if (url != null) onNavToLinkedin(url)
                    }) {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                                .data(connection.pfp_url ?: R.drawable.baseline_account_circle)
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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