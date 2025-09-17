package com.quartier.quartier

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quartier.quartier.connections.ConnectionsScreen
import com.quartier.quartier.profile.EditProfileScreen
import com.quartier.quartier.auth.LoginScreen
import com.quartier.quartier.profile.ProfileScreen
import com.quartier.quartier.auth.RegisterScreen
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.Serializable
import androidx.core.net.toUri

//App navigation using Compose Navigation, each screen is defined by an object

interface Screen {
    @Serializable
    object Login: Screen
    @Serializable
    object Register: Screen
    @Serializable
    object Connections: Screen
    @Serializable
    object Profile: Screen
}

@Composable
fun Navigation(viewModel: SessionViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    val isLoggedIn = viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    //The app entry point depends on the login status, behavior is defined in the SessionViewModel
    val startDestination = if(isLoggedIn.value) {
        Screen.Connections
    } else {
        Screen.Login
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

        composable<Screen.Connections> {
            ConnectionsScreen(onNavToProfile = {
                navController.navigate(Screen.Profile)
            }, onNavToLinkedin = { url ->
                //Open the linkedin url
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            })
        }

        composable<Screen.Profile> {
            ProfileScreen(onNavToConnections = {
                navController.navigate(Screen.Connections)
            }, onNavToLinkedin = { url ->
                //Open the linkedin url
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            })
        }
    }
}