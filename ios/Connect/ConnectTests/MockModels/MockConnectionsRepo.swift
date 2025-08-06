//
//  MockConnectionsRepo.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
@testable import Quartier

//Mock the behavior of the connections table in the supabase DB

class MockConnectionsRepo : ConnectionsRepository {
    private(set) var connections = [
        Connection(requested_by: "0", requested_for: "1", status: "accepted"),
        Connection(requested_by: "2", requested_for: "0", status: "accepted"),
        Connection(requested_by: "0", requested_for: "3", status: "pending"),
        Connection(requested_by: "4", requested_for: "0", status: "pending"),
        Connection(requested_by: "0", requested_for: "5", status: "invalid_status")
    ]
    
    func getConnections() async throws -> [Connection] { //Normally should filter for ones containing the user ID ("0") but for the sake of simplicity it is omitted
        return connections
    }
    
    func getConnectionWithUser(requestedID: String) async throws -> Connection? {
        return connections.first(where: {
            ($0.requested_by == "0" && $0.requested_for == requestedID) ||
            ($0.requested_by == requestedID && $0.requested_for == "0")
        })
    }
    
    func requestConnection(requestedID: String) async throws -> ConnectionResult? { //Create connection request
        if(requestedID == "0") { return .cannotConnectWithSelf }
        connections.append(Connection(requested_by: "0", requested_for: requestedID, status: "pending"))
        return .requested
    }
    
    func acceptConnection(requestedID: String) async throws { //Accept connection request
        let nullableIndex = connections.firstIndex(where: {
            $0.requested_by == requestedID && $0.requested_for == "0"
        })
        if let index = nullableIndex {
            connections[index] = Connection(requested_by: requestedID, requested_for: "0", status: "accepted")
        }
    }
    
    func deleteConnection(requestedID: String) async throws { //Delete connection request
        connections.removeAll {
            $0.requested_by == requestedID && $0.requested_for == "0" && $0.status == "pending"
        }
    }
}
