package com.quartier.quartier.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quartier.quartier.R
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.UploadStatus
import com.quartier.quartier.database.User
import com.quartier.quartier.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: EditProfileViewModel = hiltViewModel(), returnToProfile: ()->Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val shownPfpUri = uiState.newPfpUri?.toString() ?: uiState.user?.pfp_url

    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if(uri != null) {
            viewModel.previewNewPfp(uri)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {
            Text("Edit profile")
        }, navigationIcon = {
            IconButton(onClick = returnToProfile) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalContext.current.getString(R.string.back))
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            EditProfileScreen(uiState.user, uiState.socials, shownPfpUri, onChangePfp = {
                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }, onSaveProfile = { name, job, linkedin ->
                viewModel.saveUser(name, job, linkedin)
            })
        }

        uiState.userMessage?.let { userMessage ->
            val snackbarText = LocalContext.current.getString(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        uiState.saveStatus?.let { status ->
            when(status) {
                UploadStatus.Success -> {
                    returnToProfile()
                }
                UploadStatus.Loading -> {
                    val snackbarText = LocalContext.current.getString(R.string.saving)
                    LaunchedEffect(snackbarHostState, viewModel, status, snackbarText) {
                        snackbarHostState.showSnackbar(snackbarText, duration = SnackbarDuration.Indefinite)
                    }
                }
                UploadStatus.Error -> {
                    val snackbarText = LocalContext.current.getString(R.string.saving_error)
                    LaunchedEffect(snackbarHostState, viewModel, status, snackbarText) {
                        snackbarHostState.showSnackbar(snackbarText)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileScreen(userData: User?, userSocials: Socials?, shownPfpUri: String?, onChangePfp: ()->Unit, onSaveProfile: (String, String, String)->Unit) {
    var newName by rememberSaveable { mutableStateOf("") }
    var newJob by rememberSaveable { mutableStateOf("") }

    var newLinkedin by rememberSaveable { mutableStateOf("") }
    var linkedinValid by rememberSaveable { mutableStateOf(true) }
    val linkedinRegex = Regex("^https://www\\.linkedin\\.com/in/[^/]+/?$")

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize()) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current)
            .data(shownPfpUri ?: R.drawable.baseline_account_circle)
            .crossfade(true)
            .build(),
            contentDescription = LocalContext.current.getString(R.string.pfp_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(150.dp).border(2.dp ,Color.Black, CircleShape).padding(2.dp).clip(CircleShape)
                .clickable(onClick = onChangePfp)
        )
        TextField(value = newName, onValueChange = {newName = it}, label = { Text(LocalContext.current.getString(R.string.name_label)) },
            placeholder = {Text(userData?.name ?: "")})
        TextField(value = newJob, onValueChange = {newJob = it}, label = { Text(LocalContext.current.getString(R.string.job_label)) },
            placeholder = {Text(userData?.job ?: "")})
        TextField(value = newLinkedin, onValueChange = {
                newLinkedin = it
                linkedinValid = newLinkedin.isEmpty() || linkedinRegex.matches(newLinkedin)
                }, label = { Text(LocalContext.current.getString(R.string.linkedin_url_label)) },
                placeholder = {Text(userSocials?.linkedin_url ?: "https://www.linkedin.com/in/[your-username]")},
                isError = !linkedinValid,
                supportingText = {
                    if(!linkedinValid) Text(LocalContext.current.getString(R.string.invalid_input))
                }
            )
        Button(onClick = {onSaveProfile(newName, newJob, newLinkedin)}, enabled = linkedinValid) {
            Text(LocalContext.current.getString(R.string.save_profile))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen(userData = User(id = "0", name = "Steve Jobs", job = "CEO"),
        userSocials = null, shownPfpUri = null, onChangePfp = {}) { _, _, _ -> }
}