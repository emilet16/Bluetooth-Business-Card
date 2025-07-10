package com.example.businesscard

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.businesscard.connections.ConnectScreen
import com.example.businesscard.connections.ConnectionsScreen
import com.example.businesscard.profile.EditProfileScreen
import com.example.businesscard.auth.LoginScreen
import com.example.businesscard.profile.ProfileScreen
import com.example.businesscard.auth.RegisterScreen
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.Serializable
import androidx.core.net.toUri

interface Screen {
    @Serializable
    object Connect: Screen
    @Serializable
    object Loading: Screen
    @Serializable
    object Login: Screen
    @Serializable
    object Register: Screen
    @Serializable
    object Connections: Screen
    @Serializable
    object Profile: Screen
    @Serializable
    object EditProfile: Screen
}

@Composable
fun Navigation(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    val navigationState = viewModel.navigationState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val startDestination = when(navigationState.value.sessionStatus) {
        is SessionStatus.Initializing -> {
            Screen.Loading
        }
        is SessionStatus.Authenticated -> {
            navigationState.value.savedScreen
        }
        else -> {
            Screen.Login
        }
    }

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Screen.Login> {
            LoginScreen(onNavToRegister = {
                navController.navigate(Screen.Register)
            })
        }

        composable<Screen.Register> {
            RegisterScreen(onNavToLogin = {
                navController.navigate(Screen.Login)
            })
        }

        composable<Screen.Connect> {
            ConnectScreen(viewModel = hiltViewModel(viewModelStoreOwner), returnToConnections = {
                navController.navigateUp()
                viewModel.navigate(Screen.Connections)
            })
        }

        composable<Screen.Connections> {
            ConnectionsScreen(onNavToConnect = {
                navController.navigate(Screen.Connect)
                viewModel.navigate(Screen.Connect)
            }, onNavToProfile = {
                navController.navigate(Screen.Profile)
                viewModel.navigate(Screen.Profile)
            }, onNavToLinkedin = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            })
        }

        composable<Screen.Profile> {
            ProfileScreen(onNavToEditProfile = {
                navController.navigate(Screen.EditProfile)
                viewModel.navigate(Screen.EditProfile)
            }, onNavToConnections = {
                navController.navigate(Screen.Connections)
                viewModel.navigate(Screen.Connections)
            }, onNavToLinkedin = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            })
        }

        composable<Screen.EditProfile> {
            EditProfileScreen(returnToProfile = {
                navController.navigateUp()
                viewModel.navigate(Screen.Profile)
            })
        }

        composable<Screen.Loading> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    Text(LocalContext.current.getString(R.string.loading), modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}