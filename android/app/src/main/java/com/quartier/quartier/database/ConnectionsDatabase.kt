package com.quartier.quartier.database

import com.quartier.quartier.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class Connection(
    val requested_by: String,
    val requested_for: String,
    val status: String
)

interface ConnectResult {
    object Pending: ConnectResult
    object Requested: ConnectResult
    object Accepted: ConnectResult
    object AlreadyConnected: ConnectResult
    class Error(val message: String): ConnectResult
}

class ConnectionsDatabase @Inject constructor(){
    suspend fun getConnections() : List<Connection> {
        val uid = supabase.auth.currentUserOrNull()!!.id
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

    suspend fun requestConnection(requestedId: String) : ConnectResult {
        val userId = supabase.auth.currentUserOrNull()!!.id
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
            acceptConnection(user2Id = requestedId)
            return ConnectResult.Accepted
        }
        return ConnectResult.Pending
    }

    suspend fun acceptConnection(user2Id: String) { //TODO: error management
        val user1Id = supabase.auth.currentUserOrNull()!!.id
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

    suspend fun deleteConnection(user2Id: String) {
        val user1Id = supabase.auth.currentUserOrNull()!!.id
        supabase.from("connections").delete() {
            filter {
                and {
                    Connection::requested_by eq user2Id
                    Connection::requested_for eq user1Id
                }
            }
        }
    }
}