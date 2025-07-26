package com.quartier.quartier.database

import androidx.lifecycle.ViewModel
import com.quartier.quartier.supabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class User(
    val id: String,
    val name: String,
    val job: String,
    val pfp_url: String? = null,
    val connectionStatus: String? = null
)

interface UserRepository {
    suspend fun getUsers(uids: List<String>): List<User>
    suspend fun getUser(): User
    suspend fun updateUser(name: String, job: String)
    suspend fun uploadPfp(fileName: String, image: ByteArray)
}
@Singleton
class UserDatabase @Inject constructor(private val authRepository: AuthRepository) : UserRepository {
    override suspend fun getUsers(uids: List<String>): List<User> {
        return supabase.from("profiles").select(columns = Columns.ALL) {
            filter {
                User::id isIn uids
            }
        }.decodeList<User>()
    }

    override suspend fun getUser(): User {
        val uid = authRepository.userId.value!!
        return supabase.from("profiles").select(columns = Columns.ALL) {
            filter {
                User::id eq uid
            }
        }.decodeSingle<User>()
    }

    override suspend fun updateUser(name: String, job: String) {
        val uid = authRepository.userId.value!!
        supabase.from("profiles").update({
            User::name setTo name
            User::job setTo job
        }) {
            filter {
                User::id eq uid
            }
        }
    }

    override suspend fun uploadPfp(fileName: String, image: ByteArray) { //TODO Delete old unused pfps
        val uid = authRepository.userId.value!!
        supabase.storage.from("pfp").upload(fileName, image)

        val url = supabase.storage.from("pfp").publicUrl(fileName)
        supabase.from("profiles").update({
            User::pfp_url setTo url
        }) {
            filter {
                User::id eq uid
            }
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserModule {
    @Binds
    abstract fun bindUserRepository(userDatabase: UserDatabase): UserRepository
}

interface UploadStatus {
    object Loading: UploadStatus
    object Success: UploadStatus
    object Error: UploadStatus
}