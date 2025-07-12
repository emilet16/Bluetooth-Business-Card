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

val supabase = createSupabaseClient(
    supabaseUrl = "https://liumhaenwcmzwargxnpv.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpdW1oYWVud2Ntendhcmd4bnB2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkwNjIzNDUsImV4cCI6MjA2NDYzODM0NX0.UtzshBOjki83CdLOFu1dyu70JKDQOgcaHtxo1WqrpwY"
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