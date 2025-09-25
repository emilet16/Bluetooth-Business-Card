//
//  ConnectionsDatabase.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-12.
//

//A class handling methods for the Connections table in supabase DB

struct Connection: Codable, Sendable, Hashable {
    var requested_by: String
    var requested_for: String
    var status: String
}

enum ConnectionResult {
    case cannotConnectWithSelf
    case requested
}

protocol ConnectionsRepository : Sendable {
    func getConnections() async throws -> [Connection]
    func getConnectionWithUser(requestedID: String) async throws -> Connection?
    func connectionsToUsers(userRepository: any UserRepository, connections: [Connection]) async throws -> [User]
    func requestConnection(requestedID: String) async throws -> ConnectionResult?
    func acceptConnection(requestedID: String) async throws
    func deleteConnection(requestedID: String) async throws
}

final class ConnectionsDatabase : ConnectionsRepository {
    static let shared = ConnectionsDatabase()
    
    func getConnections() async throws -> [Connection] { //Get all connections for the user
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        return try await supabase.from("connections").select("*").or("and(requested_by.eq.\(userID),status.eq.accepted),requested_for.eq.\(userID)").execute().value as [Connection]
    }
    
    func getConnectionWithUser(requestedID: String) async throws -> Connection? { //Get connection with another user
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        return try await (supabase.from("connections").select("*")
            .or("and(requested_for.eq.\(userID), requested_by.eq.\(requestedID)), and(requested_by.eq.\(userID), requested_for.eq.\(requestedID))")
            .execute().value as [Connection]).first
    }
    
    func connectionsToUsers(userRepository: any UserRepository, connections: [Connection]) async throws -> [User] {
        let userID = supabase.auth.currentUser?.id.uuidString
        
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        var connectedUserStatus: [String: String] = [:]
        for connection in connections {
            let connectedID = connection.requested_by == userID.lowercased() ? connection.requested_for : connection.requested_by
            connectedUserStatus[connectedID] = connection.status
        }
        
        let users = try await userRepository.getUsers(ids: Array(connectedUserStatus.keys))
        return users.map({ user in
            User(id: user.id, name: user.name, job: user.job, pfp_url: user.pfp_url, connectionStatus: connectedUserStatus[user.id])
        })
    }
    
    func requestConnection(requestedID: String) async throws -> ConnectionResult? { //Create a connection request
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        if(userID.lowercased() == requestedID) { return .cannotConnectWithSelf }
        
        try await supabase.from("connections").upsert(Connection(requested_by: userID, requested_for: requestedID, status: "pending")).execute()
        return .requested
    }
    
    func acceptConnection(requestedID: String) async throws { //Accept a connection request
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        try await supabase.from("connections").update(["status":"accepted"]).eq("requested_by", value: requestedID).eq("requested_for", value: userID).execute()
    }
    
    func deleteConnection(requestedID: String) async throws { //Delete a connection request
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        try await supabase.from("connections").delete().eq("requested_by", value: requestedID).eq("requested_for", value: userID).execute()
    }
}
