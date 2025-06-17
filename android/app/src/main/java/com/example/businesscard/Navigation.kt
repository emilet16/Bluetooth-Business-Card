package com.example.businesscard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.businesscard.connect.ConnectScreen
import com.example.businesscard.connections.ConnectionsScreen
import com.example.businesscard.editprofile.EditProfileScreen
import com.example.businesscard.login.LoginScreen
import com.example.businesscard.profile.ProfileScreen
import com.example.businesscard.register.RegisterScreen
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.Serializable

@Serializable
object Home
@Serializable
object Loading
@Serializable
object Login
@Serializable
object Register
@Serializable
object Connections
@Serializable
object Profile
@Serializable
object EditProfile

@Composable
fun Navigation(viewModel: NavigationViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    val sessionStatus = viewModel.sessionStatus.collectAsStateWithLifecycle()

    val startDestination = when(sessionStatus.value) {
        is SessionStatus.Initializing -> {
            Loading
        }
        is SessionStatus.Authenticated -> {
            Home
        }
        else -> {
            Login
        }
    }

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    val uriHandler = LocalUriHandler.current

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Login> {
            LoginScreen(onNavToRegister = {
                navController.navigate(Register)
            })
        }

        composable<Register> {
            RegisterScreen(onNavToLogin = {
                navController.navigate(Login)
            })
        }

        composable<Home> {
            ConnectScreen(viewModel = hiltViewModel(viewModelStoreOwner), onNavToProfile = {
                navController.navigate(Profile)
            }, onNavToConnections = {
                navController.navigate(Connections)
            })
        }

        composable<Connections> {
            ConnectionsScreen(onNavToHome = {
                navController.navigate(Home)
            }, onNavToProfile = {
                navController.navigate(Profile)
            }, onNavToLinkedin = {
                uriHandler.openUri(it)
            })
        }

        composable<Profile> {
            ProfileScreen(onNavToEditProfile = {
                navController.navigate(EditProfile)
            }, onNavToHome = {
                navController.navigate(Home)
            }, onNavToConnections = {
                navController.navigate(Connections)
            })
        }

        composable<EditProfile> {
            EditProfileScreen(returnToLastScreen = {
                navController.navigateUp()
            })
        }

        composable<Loading> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    Text(LocalContext.current.getString(R.string.loading), modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}