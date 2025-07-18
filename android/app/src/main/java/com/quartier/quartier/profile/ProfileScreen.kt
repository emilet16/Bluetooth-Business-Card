package com.quartier.quartier.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.quartier.quartier.R
import com.quartier.quartier.database.Socials
import com.quartier.quartier.components.MainBottomAppBar
import com.quartier.quartier.components.SelectedScreen
import com.quartier.quartier.database.User
import com.quartier.quartier.components.UserCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), onNavToEditProfile: ()->Unit, onNavToConnections: ()->Unit, onNavToLinkedin: (String)->Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {
            Text(LocalContext.current.getString(R.string.profile_title))
        }, actions = {
            IconButton(onClick = {
                onNavToEditProfile()
            }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = LocalContext.current.getString(R.string.edit_profile),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        })
    }, bottomBar = {
        MainBottomAppBar(onNavToConnections = onNavToConnections, selectedScreen = SelectedScreen.Profile)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ProfileScreen(uiState.isRefreshing, uiState.user, uiState.socials, onNavToLinkedin = onNavToLinkedin, onRefresh = {viewModel.refreshUser()})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(isRefreshing: Boolean, userData: User?, socials: Socials?, onRefresh: ()->Unit, onNavToLinkedin: (String)->Unit) {
    PullToRefreshBox(isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            if(userData != null) {
                item {
                    UserCard(onClick = {
                        val url = socials?.linkedin_url
                        if (url != null) onNavToLinkedin(url)
                    }, user = userData)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(isRefreshing = false, userData = User("0", "Steve Jobs", "CEO"), socials = null, onRefresh = {}) { }
}