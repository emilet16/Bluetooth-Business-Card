package com.quartier.quartier.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quartier.quartier.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//Compose function defining a screen for users to edit their profiles
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    shownPfpUri: String?,
    onDismiss: ()->Unit,
    onPreviewPfp: (Uri) -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    linkedinRegexCheck: (String) -> Boolean,
    snackbarHostState: SnackbarHostState,
    sheetState: SheetState,
    scope: CoroutineScope) {

    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if(uri != null) {
            onPreviewPfp(uri)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        modifier = Modifier.padding(WindowInsets.systemBars.only(WindowInsetsSides.Top).asPaddingValues())) {
        EditProfileScreen(shownPfpUri, onChangePfp = {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }, onSaveProfile = onSaveProfile,
            matchesLinkedinRegex = linkedinRegexCheck,
            onClose = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
        )

        SnackbarHost(snackbarHostState)
    }
}

@Composable
private fun EditProfileScreen(shownPfpUri: String?,
                              onChangePfp: ()->Unit, onSaveProfile: (String, String, String)->Unit,
                              matchesLinkedinRegex: (String)-> Boolean, onClose: () -> Unit) {
    var newName by rememberSaveable { mutableStateOf("") }
    var newJob by rememberSaveable { mutableStateOf("") }

    var newLinkedin by rememberSaveable { mutableStateOf("") }
    var linkedinValid by rememberSaveable { mutableStateOf(true) }

    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .padding(20.dp)) {

        item {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                .data(shownPfpUri ?: R.drawable.baseline_account_circle)
                .crossfade(true)
                .build(),
                contentDescription = LocalContext.current.getString(R.string.pfp_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .innerShadow(CircleShape, Shadow(1.dp, alpha = .25f))
                    .clip(CircleShape)
                    .clickable(onClick = onChangePfp)
            )
        }

        item {
            OutlinedTextField(newName, onValueChange = {
                newName = it
            }, label = {Text(LocalContext.current.getString(R.string.name_label))}, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth())
        }

        item {
            OutlinedTextField(newJob, onValueChange = {
                newJob = it
            }, label = {Text(LocalContext.current.getString(R.string.job_label))}, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth())
        }

        item {
            OutlinedTextField(newLinkedin, onValueChange = {
                newLinkedin = it
                linkedinValid = newLinkedin.isEmpty() || matchesLinkedinRegex(newLinkedin)
            }, label = {Text(LocalContext.current.getString(R.string.linkedin_url_label))},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = !linkedinValid,
                supportingText = {
                    Text("https://www.linkedin.com/in/[your-username]")
                })
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onClose,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)) {
                    Text(LocalContext.current.getString(R.string.dont_save_profile))
                }

                Button(onClick = {onSaveProfile(newName, newJob, newLinkedin)}, enabled = linkedinValid,
                    modifier = Modifier.weight(1f)) {
                    Text(LocalContext.current.getString(R.string.save_profile))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen(shownPfpUri = null, onChangePfp = {},
        onSaveProfile = { _, _, _ -> }, matchesLinkedinRegex = {_ -> true},
        onClose = {})
}