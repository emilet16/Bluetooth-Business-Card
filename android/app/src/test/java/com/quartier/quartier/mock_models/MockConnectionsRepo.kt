package com.quartier.quartier.mock_models

import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.ConnectionRequestResult
import com.quartier.quartier.database.ConnectionsRepository
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserRepository

//Contains fake connections, test behavior for all of them, imitates the Connections table in DB

class MockConnectionsRepo() : ConnectionsRepository {
    private val connections: MutableList<Connection> = mutableListOf(
        Connection("0", "1", "accepted"),
        Connection("2", "0", "accepted"),
        Connection("0", "3", "pending"),
        Connection("4", "0", "pending"),
        Connection("0", "5", "invalid_status"))

    override suspend fun getConnections(): List<Connection> {return connections.toList()}

    override suspend fun getConnectionWithUser(requestedId: String): Connection? {
        return connections.find {
            (it.requested_by == "0" && it.requested_for == requestedId) || (it.requested_by == requestedId && it.requested_for == "0")
        }
    }

    override suspend fun connectionsToUsers(connections: List<Connection>, userRepository: UserRepository) : List<User> {
        val uid = "0"

        val connectionsMap = connections.associateBy(
            {if (it.requested_by == uid) it.requested_for else it.requested_by}, //find the other user's id
            {it.status}
        )
        val users = userRepository.getUsers(connectionsMap.keys.toList())
        return users.map { user ->
            User(user.id, user.name, user.job, user.pfp_url, connectionsMap[user.id])
        }
    }

    override suspend fun requestConnection(requestedId: String) : ConnectionRequestResult {
        if(requestedId == "0") return ConnectionRequestResult.CannotConnectWithSelf
        connections.add(Connection("0", requestedId, "pending"))
        return ConnectionRequestResult.Success
    }

    override suspend fun acceptConnection(user2Id: String) {
        val index = connections.indexOfFirst {
            it.requested_by == user2Id && it.requested_for == "0"
        }
        if(index != -1) {
            connections[index] = Connection(user2Id, "0", "accepted")
        }
    }

    override suspend fun deleteConnection(user2Id: String) {
        connections.remove(Connection(user2Id, "0", "pending"))
    }
}