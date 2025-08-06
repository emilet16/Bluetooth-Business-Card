package com.quartier.quartier.database

import com.quartier.quartier.supabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

//Class to interface with the Connections table in the supabase database

@Serializable
data class Connection(
    val requested_by: String,
    val requested_for: String,
    val status: String
)

interface ConnectionsRepository {
    suspend fun getConnections(): List<Connection>
    suspend fun getConnectionWithUser(requestedId: String): Connection?
    suspend fun requestConnection(requestedId: String) : ConnectionRequestResult
    suspend fun acceptConnection(user2Id: String)
    suspend fun deleteConnection(user2Id: String)
}
@Singleton
class ConnectionsDatabase @Inject constructor(private val authRepository: AuthRepository) : ConnectionsRepository{
    override suspend fun getConnections() : List<Connection> {
        val uid = authRepository.userId.value!!
        return supabase.from("connections").select(Columns.ALL) {
            filter {
                or{
                    and{
                        Connection::requested_by eq uid //only show the connections requested for the user, not the ones sent by them
                        Connection::status eq "accepted"
                    }
                    Connection::requested_for eq uid //show all accepted connections
                }
            }
        }.decodeList()
    }

    override suspend fun getConnectionWithUser(requestedId: String) : Connection? {
        val userId = authRepository.userId.value!!
        return supabase.from("connections").select(Columns.ALL) {
            filter {
                or { //Try to any connections matching both users, no matter who requested it
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
    }

    override suspend fun requestConnection(requestedId: String) : ConnectionRequestResult {
        val userId = authRepository.userId.value!!
        if(userId == requestedId) return ConnectionRequestResult.CannotConnectWithSelf //Prevent the user from making a connection with themselves
        supabase.from("connections").upsert(Connection(userId, requestedId, "pending")) {
            ignoreDuplicates = true
        }
        return ConnectionRequestResult.Success
    }

    override suspend fun acceptConnection(user2Id: String) { //TODO: error management
        val user1Id = authRepository.userId.value!!
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

    override suspend fun deleteConnection(user2Id: String) {
        val user1Id = authRepository.userId.value!!
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

interface ConnectionRequestResult {
    object Success : ConnectionRequestResult
    object CannotConnectWithSelf: ConnectionRequestResult
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ConnectionsModule {
    @Binds
    abstract fun bindConnectionsRepository(connectionsDatabase: ConnectionsDatabase): ConnectionsRepository
}