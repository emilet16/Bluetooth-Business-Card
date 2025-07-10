package com.example.businesscard

import android.net.Uri
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
    val pfp_url: String? = null,
    val connectionStatus: String? = null
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

class UserRepository @Inject constructor(private val imageManager: ImageManager) {
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

    suspend fun getUserSocials(id: String): Socials {
        return supabase.from("socials").select(columns = Columns.ALL) {
            filter {
                Socials::id eq id
            }
        }.decodeSingle()
    }

    suspend fun getUserSocialsList() : List<Socials> {
        return supabase.from("socials").select(columns = Columns.ALL).decodeList<Socials>() //RLS returns all friended users
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

    suspend fun upsertSocials(uid: String, linkedinURL: String) : UploadStatus {
        try {
            supabase.from("socials").upsert(Socials(id = uid, linkedin_url = linkedinURL)) {
                onConflict = "id"
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }

    suspend fun uploadPfp(uid: String, fileName: String, uri: Uri) : UploadStatus { //TODO Delete old unused pfps
        try {
            val image = imageManager.preparePfpForUpload(uri)
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

    suspend fun requestConnection(userId: String, requestedId: String) : ConnectResult {
        var currentConnection: Connection? = null
        try {
            currentConnection = supabase.from("connections").select(Columns.ALL) {
                filter {
                    or {
                        and {
                            Connection::requested_by eq userId
                            Connection::requested_for eq requestedId
                        }
                        and {
                            Connection::requested_by eq requestedId
                            Connection::requested_for eq userId
                        }
                    }
                }
            }.decodeSingleOrNull()
        } catch(e: Exception) {
            return ConnectResult.Error(e.message ?: "Unknown error")
        }

        if(currentConnection == null) {
            supabase.from("connections").upsert(Connection(userId, requestedId, "pending")) {
                ignoreDuplicates = true
            }
            return ConnectResult.Requested
        } else if(currentConnection.status == "accepted") {
            return ConnectResult.AlreadyConnected
        } else if(currentConnection.status == "pending" && currentConnection.requested_by == requestedId) { //Accept the request
            acceptConnection(user1Id = userId, user2Id = requestedId)
            return ConnectResult.Accepted
        }
        return ConnectResult.Pending
    }

    suspend fun acceptConnection(user1Id: String, user2Id: String) { //TODO: error management
        supabase.from("connections").update({
            Connection::status setTo "accepted"
        }) {
            filter {
                and {
                    Connection::requested_by eq user2Id
                    Connection::requested_for eq user1Id
                }
            }
        }
    }

    suspend fun deleteConnection(user1Id: String, user2Id: String) {
        supabase.from("connections").delete() {
            filter {
                and {
                    Connection::requested_by eq user2Id
                    Connection::requested_for eq user1Id
                }
            }
        }
    }

    private suspend fun getConnections(uid: String) : List<Connection> {
        return supabase.from("connections").select(Columns.ALL) {
            filter {
                or{
                    and{
                        Connection::requested_by eq uid
                        Connection::status eq "accepted"
                    }
                    Connection::requested_for eq uid
                }
            }
        }.decodeList()
    }

    suspend fun getConnectedUsers(uid: String) : List<User> {
        val connections = getConnections(uid)
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

interface ConnectResult {
    object Pending: ConnectResult
    object Requested: ConnectResult
    object Accepted: ConnectResult
    object AlreadyConnected: ConnectResult
    class Error(val message: String): ConnectResult
}