package com.quartier.quartier.database

import com.quartier.quartier.supabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

//Class to interface with the profiles table in the supabase database

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
    suspend fun getUser(): User?
    suspend fun updateUser(name: String, job: String)
    suspend fun uploadPfp(fileName: String, image: ByteArray)
}
@Singleton
class UserDatabase @Inject constructor() : UserRepository {
    override suspend fun getUsers(uids: List<String>): List<User> {
        return supabase.from("profiles").select(columns = Columns.ALL) { //Get a list of profiles from the user IDs.
            filter {
                User::id isIn uids
            }
        }.decodeList<User>()
    }

    override suspend fun getUser(): User? {
        val uid = supabase.auth.currentUserOrNull()?.id

        if(uid == null) throw SupabaseException("No internet connection!")

        return supabase.from("profiles").select(columns = Columns.ALL) { //Get the user's profile
            filter {
                User::id eq uid
            }
        }.decodeSingleOrNull()
    }

    override suspend fun updateUser(name: String, job: String) {
        val uid = supabase.auth.currentUserOrNull()?.id

        if(uid == null) throw SupabaseException("No internet connection!")

        supabase.from("profiles").update({ //Edit the user's profile
            User::name setTo name
            User::job setTo job
        }) {
            filter {
                User::id eq uid
            }
        }
    }

    override suspend fun uploadPfp(fileName: String, image: ByteArray) {
        val uid = supabase.auth.currentUserOrNull()?.id

        if(uid == null) throw SupabaseException("No internet connection!")

        supabase.storage.from("pfp").upload(fileName, image) //Upload the pfp to the storage bucket

        val url = supabase.storage.from("pfp").publicUrl(fileName) //Get the public URL to the image
        supabase.from("profiles").update({ //Add the url to the user's profile
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