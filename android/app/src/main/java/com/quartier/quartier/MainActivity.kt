package com.quartier.quartier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quartier.quartier.ui.theme.QuarierTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

//Main activity, defined global scope vars

val supabase = createSupabaseClient(
    supabaseUrl = "https://liumhaenwcmzwargxnpv.supabase.co",
    supabaseKey = "sb_publishable_YVhSNGzOosROZp8bY-qzPQ_xJafBP1M"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}

const val TAG: String = "Quartier"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            QuarierTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface) {
                    Navigation()
                }
            }
        }
    }
}