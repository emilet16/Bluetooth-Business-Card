package com.quartier.quartier.mock_models

import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.ConnectionRequestResult
import com.quartier.quartier.database.ConnectionsRepository

//Contains fake connections, test behavior for all of them, imitates the Connections table in DB

class MockConnectionsRepo : ConnectionsRepository {
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