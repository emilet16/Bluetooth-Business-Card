package com.quartier.quartier.database

import com.quartier.quartier.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class User(
    val id: String,
    val name: String,
    val job: String,
    val pfp_url: String? = null,
    val connectionStatus: String? = null
)

class UserDatabase @Inject constructor() {
    suspend fun getUsers(uids: List<String>): List<User> {
        return supabase.from("profiles").select(columns = Columns.ALL) {
            filter {
                User::id isIn uids
            }
        }.decodeList<User>()
    }

    suspend fun getUser(uid: String): User {
        return supabase.from("profiles").select(columns = Columns.ALL) {
            filter {
                User::id eq uid
            }
        }.decodeSingle<User>()
    }

    suspend fun updateUser(uid: String, name: String, job: String) : UploadStatus {
        try {
            supabase.from("profiles").update({
                User::name setTo name
                User::job setTo job
            }) {
                filter {
                    User::id eq uid
                }
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }

    suspend fun uploadPfp(uid: String, fileName: String, image: ByteArray) : UploadStatus { //TODO Delete old unused pfps
        try {
            supabase.storage.from("pfp").upload(fileName, image)

            val url = supabase.storage.from("pfp").publicUrl(fileName)
            supabase.from("profiles").update({
                User::pfp_url setTo url
            }) {
                filter {
                    User::id eq uid
                }
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }
    suspend fun getConnectedUsers(uid: String, connections: List<Connection>) : List<User> {
        val connectionsMap = connections.associateBy(
            {if (it.requested_by == uid) it.requested_for else it.requested_by}, //find the other user's id
            {it.status}
        )

        val users = getUsers(connectionsMap.keys.toList())
        return users.map { user ->
            User(user.id, user.name, user.job, user.pfp_url, connectionsMap[user.id])
        }
    }
}

interface UploadStatus {
    object Loading: UploadStatus
    object Success: UploadStatus
    object Error: UploadStatus
}