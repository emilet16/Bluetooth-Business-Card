package com.quartier.quartier.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quartier.quartier.R
import com.quartier.quartier.components.EmailTextField
import com.quartier.quartier.components.PasswordTextField
import com.quartier.quartier.ui.theme.Typography

//The registration screen for the app

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavToLogin: () -> Unit
) {
    val userMessage by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            RegisterScreen(onEmailSignUp = { name, email, pwd ->
                viewModel.emailSignUp(name, email, pwd)
            }, onNavToLogin = onNavToLogin, matchesEmailRegex = {
                viewModel.matchesEmailRegex(it)
            })

            //Notify the user if something goes wrong during registration (ex. the user already exists)
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
private fun RegisterScreen(onEmailSignUp: (String, String, String) -> Unit, onNavToLogin: () -> Unit, matchesEmailRegex: (String)->Boolean) {
    var name by rememberSaveable { mutableStateOf("") }
    var nameValid by rememberSaveable { mutableStateOf(false) }

    var email by rememberSaveable { mutableStateOf("") }
    var emailValid by rememberSaveable { mutableStateOf(false) }

    var pwd by rememberSaveable { mutableStateOf("") }
    var pwdValid by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        Text(LocalContext.current.getString(R.string.greeting_new_user), style = Typography.headlineMedium, textAlign = TextAlign.Left)
        Text(LocalContext.current.getString(R.string.register_request), style = Typography.headlineMedium, textAlign = TextAlign.Left, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(15.dp))

        OutlinedTextField(name, onValueChange = {
            name = it
            nameValid = it.isNotBlank()
        }, label = {Text(LocalContext.current.getString(R.string.name_label))}, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            supportingText = {
                if(!nameValid && name.isNotEmpty()) Text(LocalContext.current.getString(R.string.invalid_input))
            }, isError = !nameValid && name.isNotEmpty(), modifier = Modifier.fillMaxWidth())

        EmailTextField(email, showError = (email.isNotEmpty() && !emailValid), onValueChange = {
            email = it
            emailValid = (it.isNotBlank() && matchesEmailRegex(it))
        })

        PasswordTextField(pwd, showError = (pwd.isNotEmpty() && !pwdValid), onValueChange = {
            pwd = it
            pwdValid = it.isNotBlank() && it.length >= 6
        })

        Spacer(Modifier.height(15.dp))

        Button({onEmailSignUp(name, email, pwd)},
            enabled = nameValid && emailValid && pwdValid, modifier = Modifier.fillMaxWidth()) {
            Text(LocalContext.current.getString(R.string.register))
        }

        TextButton(onClick = onNavToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(LocalContext.current.getString(R.string.login_suggest))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(onEmailSignUp = {_, _, _ ->}, onNavToLogin = {}, matchesEmailRegex = {_ -> true})
}