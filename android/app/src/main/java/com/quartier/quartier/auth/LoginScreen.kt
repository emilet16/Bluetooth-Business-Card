package com.quartier.quartier.auth

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quartier.quartier.R
import com.quartier.quartier.components.EmailTextField
import com.quartier.quartier.components.PasswordTextField
import com.quartier.quartier.ui.theme.Typography
import java.nio.file.WatchEvent

//The login screen for the app

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel(), onNavToRegister: () -> Unit, snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val userMessage by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LoginScreen(onEmailSignIn = { email, pwd ->
                viewModel.emailSignIn(email, pwd)
            }, onNavToRegister = onNavToRegister, matchesEmailRegex = {
                viewModel.matchesEmailRegex(it)
            })

            //Tell the user if something happened during login (ex. wrong password)
            userMessage?.let { userMessage ->
                val snackbarText = LocalContext.current.getString(userMessage)
                LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                    viewModel.snackbarMessageShown()
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(onEmailSignIn: (String, String) -> Unit, onNavToRegister: () -> Unit, matchesEmailRegex: (String)->Boolean) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailValid by rememberSaveable { mutableStateOf(false) }

    var pwd by rememberSaveable { mutableStateOf("") }
    var pwdValid by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        Text(LocalContext.current.getString(R.string.greeting),
            style = Typography.headlineSmall, textAlign = TextAlign.Left)
        Text(LocalContext.current.getString(R.string.signin_request),
            style = Typography.headlineSmall, textAlign = TextAlign.Left, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(15.dp))

        EmailTextField(email, showError = (email.isNotEmpty() && !emailValid), onValueChange = {
            email = it
            emailValid = (it.isNotBlank() && matchesEmailRegex(it))
        })

        PasswordTextField(pwd, showError = (pwd.isNotEmpty() && !pwdValid), onValueChange = {
            pwd = it
            pwdValid = it.isNotBlank() && it.length >= 6
        })

        Spacer(Modifier.height(15.dp))

        Button(onClick = {
            onEmailSignIn(email, pwd)
        }, enabled = emailValid && pwdValid, modifier = Modifier.fillMaxWidth()) {
            Text(LocalContext.current.getString(R.string.signin))
        }
        TextButton(onClick = onNavToRegister,
            modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(LocalContext.current.getString(R.string.register_suggestion))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(onEmailSignIn = {_, _ -> }, onNavToRegister = {}, matchesEmailRegex = {_ -> true})
}