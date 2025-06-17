package com.example.businesscard.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.businesscard.components.MainBottomAppBar
import com.example.businesscard.components.SelectedScreen
import com.example.businesscard.supabase.User


@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), onNavToEditProfile: ()->Unit, onNavToHome: ()->Unit, onNavToConnections: ()->Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.refreshUser()
        }
    }

    val userData by viewModel.userState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
        MainBottomAppBar(onNavToHome = onNavToHome, onNavToConnections = onNavToConnections, selectedScreen = SelectedScreen.Profile)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ProfileScreen(userData, onEditProfile = {
                onNavToEditProfile()
            })
        }
    }
}

@Composable
private fun ProfileScreen(userData: User?, onEditProfile: ()->Unit) {
    Card {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                .data(userData?.pfp_url ?: R.drawable.baseline_account_circle)
                .crossfade(true)
                .build(),
                contentDescription = LocalContext.current.getString(R.string.pfp_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(150.dp).border(2.dp ,Color.Black, CircleShape).padding(2.dp).clip(CircleShape)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(userData?.name ?: "")
                Text(userData?.job ?: "")
            }
            IconButton(onClick = onEditProfile, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Default.Create, contentDescription = LocalContext.current.getString(R.string.edit_profile))
            }
        }
    }
}