package com.example.businesscard.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.businesscard.R
import com.example.businesscard.Socials
import com.example.businesscard.components.MainBottomAppBar
import com.example.businesscard.components.SelectedScreen
import com.example.businesscard.User
import com.example.businesscard.components.UserCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), onNavToEditProfile: ()->Unit, onNavToConnections: ()->Unit, onNavToLinkedin: (String)->Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.refreshUser()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {
            Text(LocalContext.current.getString(R.string.profile_title))
        }, actions = {
            IconButton(onClick = {
                onNavToEditProfile()
            }, modifier = Modifier
                .background(Color.White, CircleShape)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = LocalContext.current.getString(R.string.edit_profile),
                    modifier = Modifier.fillMaxSize(), tint = Color.Blue
                )
            }
        })
    }, bottomBar = {
        MainBottomAppBar(onNavToConnections = onNavToConnections, selectedScreen = SelectedScreen.Profile)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ProfileScreen(uiState.user, uiState.socials, onNavToLinkedin = onNavToLinkedin)
        }
    }
}

@Composable
private fun ProfileScreen(userData: User?, socials: Socials?, onNavToLinkedin: (String)->Unit) {
    Column(modifier = Modifier.height(200.dp)) {
        if(userData != null) {
            UserCard(onClick = {
                val url = socials?.linkedin_url
                if (url != null) onNavToLinkedin(url)
            }, user = userData)
        } else {
            Text("Loading...")
        }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(userData = User("0", "Steve Jobs", "CEO"), socials = null) { }
}