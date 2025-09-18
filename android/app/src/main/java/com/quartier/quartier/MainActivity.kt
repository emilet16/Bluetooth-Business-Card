package com.quartier.quartier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quartier.quartier.ui.theme.QuartierTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.MemoryResumableCache

//Main activity, defined global scope vars

//The Supabase client connects to prod server on release builds and locally hosted on debug builds
//The url/api key can be changed in the environment vars in build.gradle.kts (:app)
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Auth) {
        if(BuildConfig.DEBUG) { //Don't cache anything for debug builds, easier testing
            sessionManager = MemorySessionManager()
            codeVerifierCache = MemoryCodeVerifierCache()
        }
    }
    install(Postgrest)
    install(Storage) {
        if(BuildConfig.DEBUG) { //Don't cache anything for debug builds, easier testing
            resumable {
                cache = MemoryResumableCache()
            }
        }
    }
}

const val TAG: String = "Quartier"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            QuartierTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface) {
                    Navigation()
                }
            }
        }
    }
}