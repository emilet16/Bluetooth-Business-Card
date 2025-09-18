package com.quartier.quartier.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quartier.quartier.R
import com.quartier.quartier.components.MainBottomAppBar
import com.quartier.quartier.components.SelectedScreen
import com.quartier.quartier.components.UserCard
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.UploadStatus
import com.quartier.quartier.database.User
import kotlinx.coroutines.launch

//Screen used to display the user's profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(),
                  onNavToConnections: ()->Unit, onNavToLinkedin: (String)->Unit,
                  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MainBottomAppBar(onNavToConnections = onNavToConnections, selectedScreen = SelectedScreen.Profile)
        }, floatingActionButton = {
            Button(onClick = {
                viewModel.showEdit()
            }) {
                Icon(Icons.Outlined.Edit, LocalContext.current.getString(R.string.edit_profile))
            }
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ProfileScreen(uiState.isRefreshing, uiState.user, uiState.socials, onNavToLinkedin = onNavToLinkedin, onRefresh = {viewModel.refreshUser()})
        }
    }

    val shownPfpUri = editState.newPfpUri?.toString() ?: uiState.user?.pfp_url

    if(editState.shown) {
        EditProfileScreen(
            shownPfpUri,
            onDismiss = {
                viewModel.hideEdit()
            },
            onPreviewPfp = { uri ->
                viewModel.previewNewPfp(uri)
            },
            onSaveProfile = { name, job, linkedin ->
                viewModel.saveUser(name, job,linkedin)
            }, linkedinRegexCheck = { linkedin ->
                viewModel.matchesLinkedinRegex(linkedin)
            }, snackbarHostState = snackbarHostState,
            sheetState = sheetState, scope = scope
        )
    }

    //Display message for the user (ex. when Bluetooth isn't available)
    uiState.userMessage?.let { userMessage ->
        val snackbarText = LocalContext.current.getString(userMessage)
        LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
            snackbarHostState.showSnackbar(snackbarText)
            viewModel.snackbarMessageShown()
        }
    }

    //Display the save status for the user & close the screen if saving is successful
    editState.saveStatus?.let { status ->
        when(status) {
            UploadStatus.Success -> {
                viewModel.snackbarMessageShown()
                scope.launch {
                    sheetState.hide()
                    viewModel.hideEdit()
                }
            }
            UploadStatus.Loading -> {
                val snackbarText = LocalContext.current.getString(R.string.saving)
                LaunchedEffect(snackbarHostState, viewModel, status, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                }
            }
            UploadStatus.Error -> {
                val snackbarText = LocalContext.current.getString(R.string.saving_error)
                LaunchedEffect(snackbarHostState, viewModel, status, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                    viewModel.snackbarMessageShown()
                }
            }
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