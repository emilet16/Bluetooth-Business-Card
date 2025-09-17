package com.quartier.quartier.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quartier.quartier.R
import com.quartier.quartier.components.Placeholder
import com.quartier.quartier.components.PublicUserCard
import com.quartier.quartier.database.User

//A screen displaying the nearby users
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyUsers(users: List<User>, onSendProfileRequest: (User)->Unit,
                onDismiss: ()->Unit,
                snackbarHostState: SnackbarHostState) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        NearbyUsers(users, onSendProfileRequest)

        SnackbarHost(snackbarHostState)
    }
}

@Composable
private fun NearbyUsers(
    users: List<User>, onSendProfileRequest: (User)->Unit
) {
    Box(modifier = Modifier.padding(20.dp)) {
        if(users.isEmpty()) {
            Placeholder(
                title = LocalContext.current.getString(R.string.alone),
                body = LocalContext.current.getString(R.string.no_nearby_users),
                icon = painterResource(R.drawable.outline_no_accounts_24)
            )
        } else {
            LazyVerticalGrid(columns = GridCells.FixedSize(150.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (user in users) {
                    item {
                        PublicUserCard(onClick = {onSendProfileRequest(user)}, user)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewNearbyUsers() {
    NearbyUsers(users = listOf<User>(User("0", "Steve Jobs", "CEO"))) { }
}