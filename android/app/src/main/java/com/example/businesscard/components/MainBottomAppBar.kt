package com.example.businesscard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.businesscard.R

interface SelectedScreen {
    object Connections: SelectedScreen
    object Profile: SelectedScreen
}

@Composable
fun MainBottomAppBar(onNavToConnections: ()->Unit = {}, onNavToProfile: ()->Unit = {}, selectedScreen: SelectedScreen) {
    var connectionsIcon = painterResource(R.drawable.outline_business_center_24)
    var profileIcon = Icons.Outlined.AccountCircle

    when(selectedScreen) {
        is SelectedScreen.Connections -> {
            connectionsIcon = painterResource(R.drawable.baseline_business_center_24)
        }
        is SelectedScreen.Profile -> {
            profileIcon = Icons.Filled.AccountCircle
        }
    }

    BottomAppBar {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavToConnections) {
                Column {
                    Icon(connectionsIcon, LocalContext.current.getString(R.string.main_app_bar_home), modifier = Modifier.size(32.dp))
                }
            }
            IconButton(onClick = onNavToProfile) {
                Column {
                    Icon(profileIcon, LocalContext.current.getString(R.string.main_app_bar_profile), modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewMainBottomAppBarConnections() {
    MainBottomAppBar(onNavToConnections = {}, onNavToProfile = {}, selectedScreen = SelectedScreen.Connections)
}

@Preview
@Composable
fun PreviewMainBottomAppBarProfile() {
    MainBottomAppBar(onNavToConnections = {}, onNavToProfile = {}, selectedScreen = SelectedScreen.Profile)
}