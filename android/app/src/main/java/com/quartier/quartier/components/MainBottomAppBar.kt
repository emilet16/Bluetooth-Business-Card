package com.quartier.quartier.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quartier.quartier.R

//The tab bar at the bottom of the app

interface SelectedScreen {
    object Connections: SelectedScreen
    object Profile: SelectedScreen
}

@Composable
fun MainBottomAppBar(onNavToConnections: ()->Unit = {}, onNavToProfile: ()->Unit = {}, selectedScreen: SelectedScreen) {
    NavigationBar {
        NavigationBarItem(selectedScreen == SelectedScreen.Connections,
            onClick = onNavToConnections, icon = {
                Icon(painterResource(R.drawable.outline_group_24),
                    LocalContext.current.getString(R.string.main_app_bar_connections),
                    modifier = Modifier.size(32.dp)
                )
            }, label = {
                Text(LocalContext.current.getString(R.string.main_app_bar_connections))
            }, alwaysShowLabel = true
        )

        NavigationBarItem(selectedScreen == SelectedScreen.Profile,
            onClick = onNavToProfile, icon = {
                Icon(Icons.Outlined.AccountCircle, LocalContext.current.getString(R.string.main_app_bar_profile),
                    modifier = Modifier.size(32.dp))
            }, label = {
                Text(LocalContext.current.getString(R.string.main_app_bar_profile))
            }, alwaysShowLabel = true
        )
    }
}

@Preview
@Composable
fun PreviewMainBottomAppBar() {
    MainBottomAppBar(onNavToConnections = {}, onNavToProfile = {}, selectedScreen = SelectedScreen.Connections)
}