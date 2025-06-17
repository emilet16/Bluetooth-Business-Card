package com.example.businesscard.supabase

import android.net.Uri
import com.example.businesscard.image.ImageManager
import com.example.businesscard.supabase
import io.github.jan.supabase.auth.auth
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
    val pfp_url: String? = null
)

@Serializable
data class Connection(
    val requested_by: String,
    val requested_for: String,
    val status: String
)

@Serializable
data class Socials(
    val id: String,
    val linkedin_url: String?
)

class UserRepository @Inject constructor(val imageManager: ImageManager) {
    suspend fun getUsers(uids: List<String>): List<User> {
        return supabase.from("profiles").select(columns = Columns.ALL) {
            filter {
                User::id isIn uids
            }
        }.decodeList<User>()
    }

    suspend fun getCurrentUser(): User {
        return supabase.from("profiles").select(columns = Columns.ALL) {
            filter {
                User::id eq supabase.auth.currentUserOrNull()!!.id
            }
        }.decodeSingle<User>()
    }

    suspend fun getUserSocials(id: String): Socials {
        return supabase.from("socials").select(columns = Columns.ALL) {
            filter {
                Socials::id eq id
            }
        }.decodeSingle()
    }

    suspend fun getUserSocialsList(ids: List<String>) : List<Socials> {
        return supabase.from("socials").select(columns = Columns.ALL) {
            filter {
                Socials::id isIn ids
            }
        }.decodeList<Socials>()
    }

    suspend fun updateUser(name: String, job: String) : UploadStatus {
        try {
            supabase.from("profiles").update({
                User::name setTo name
                User::job setTo job
            }) {
                filter {
                    User::id eq supabase.auth.currentUserOrNull()!!.id
                }
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }

    suspend fun upsertSocials(linkedinURL: String) : UploadStatus {
        try {
            supabase.from("socials").upsert(Socials(id = supabase.auth.currentUserOrNull()!!.id, linkedin_url = linkedinURL)) {
                onConflict = "id"
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }

    suspend fun uploadPfp(fileName: String, uri: Uri) : UploadStatus { //TODO Delete old unused pfps
        try {
            val image = imageManager.preparePfpForUpload(uri)
            supabase.storage.from("pfp").upload(fileName, image)

            val url = supabase.storage.from("pfp").publicUrl(fileName)
            supabase.from("profiles").update({
                User::pfp_url setTo url
            }) {
                filter {
                    User::id eq supabase.auth.currentUserOrNull()!!.id
                }
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }

    suspend fun requestConnection(requestedId: String) : ConnectResult {
        var currentConnection: Connection? = null
        try {
            currentConnection = supabase.from("connections").select(Columns.ALL) {
                filter {
                    or {
                        and {
                            Connection::requested_by eq supabase.auth.currentUserOrNull()!!.id
                            Connection::requested_for eq requestedId
                        }
                        and {
                            Connection::requested_by eq requestedId
                            Connection::requested_for eq supabase.auth.currentUserOrNull()!!.id
                        }
                    }
                }
            }.decodeSingleOrNull()
        } catch(e: Exception) {
            return ConnectResult.Error(e.message ?: "Unknown error")
        }

        if(currentConnection == null) {
            supabase.from("connections").upsert(Connection(supabase.auth.currentUserOrNull()!!.id, requestedId, "pending")) {
                ignoreDuplicates = true
            }
            return ConnectResult.Requested
        } else if(currentConnection.status == "accepted") {
            return ConnectResult.AlreadyConnected
        } else if(currentConnection.status == "pending" && currentConnection.requested_by == requestedId) { //Accept the request
            acceptConnection(requestedId)
            return ConnectResult.Accepted
        }
        return ConnectResult.Pending
    }

    suspend fun acceptConnection(userId: String) { //TODO: error management
        supabase.from("connections").update({
            Connection::status setTo "accepted"
        }) {
            filter {
                and {
                    Connection::requested_by eq userId
                    Connection::requested_for eq supabase.auth.currentUserOrNull()!!.id
                }
            }
        }
    }

    suspend fun deleteConnection(userId: String) {
        supabase.from("connections").delete() {
            filter {
                and {
                    Connection::requested_by eq userId
                    Connection::requested_for eq supabase.auth.currentUserOrNull()!!.id
                }
            }
        }
    }

    suspend fun getConnectedUsers() : Map<String, List<User>>{
        val currentUserID = supabase.auth.currentUserOrNull()!!.id

        val connections: List<Connection> = supabase.from("connections").select(Columns.ALL) {
            filter {
                or{
                    Connection::requested_by eq currentUserID
                    Connection::requested_for eq currentUserID
                }
            }
        }.decodeList()

        val connectionIds: Map<String, MutableList<String>> = mapOf(Pair("pending", mutableListOf<String>()), Pair("accepted", mutableListOf<String>())) //maybe encapsulate status inside a class

        for(connection in connections) {
            if(connection.requested_by == currentUserID) {
                if(connection.status == "accepted") connectionIds[connection.status]!!.add(connection.requested_for) //In the future maybe show sent pending connection requests in another place
            } else {
                connectionIds[connection.status]!!.add(connection.requested_by)
            }
        }

        return connectionIds.mapValues { userIds ->
            getUsers(userIds.value)
        }
    }
}

interface UploadStatus {
    object Loading: UploadStatus
    object Success: UploadStatus
    object Error: UploadStatus
}

interface ConnectResult {
    object Pending: ConnectResult
    object Requested: ConnectResult
    object Accepted: ConnectResult
    object AlreadyConnected: ConnectResult
    class Error(val message: String): ConnectResult
}