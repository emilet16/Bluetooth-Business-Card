package com.quartier.quartier.database

import com.quartier.quartier.supabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

//This class holds the user ID for the entire app
//Work around weird bugs with supabase when the app is in background making it crash

interface AuthRepository {
    val userId: StateFlow<String?>
    fun updateUserId(newUserId: String)
}

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    private val _userId = MutableStateFlow<String?>(null)
    override val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        _userId.value = supabase.auth.currentUserOrNull()?.id
    }

    override fun updateUserId(newUserId: String) {
        _userId.value = newUserId
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepoModule {
    @Binds
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository
}