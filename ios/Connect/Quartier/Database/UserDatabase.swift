//
//  UserRepository.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import Supabase
import Foundation

struct User : Decodable, Hashable {
    var id: String
    var name: String
    var job: String
    var pfp_url: String?
    var connectionStatus: String? = nil
}

class UserDatabase {
    static let shared = UserDatabase()
    
    func getUser() async throws -> User {
        let id = supabase.auth.currentUser!.id.uuidString
        return try await (supabase.from("profiles").select("*").eq("id", value: id).execute().value as [User]).first!
    }
    
    func getUsers(ids: [String]) async throws -> [User] {
        return try await supabase.from("profiles").select("*").in("id", values: ids).execute().value
    }
    
    func getConnectedUsers(connections: [Connection]) async throws -> [User] {
        let userID = supabase.auth.currentUser!.id.uuidString
        let connections = try await ConnectionsDatabase.shared.getConnections()
        var connectedUserStatus: [String: String] = [:]
        for connection in connections {
            let connectedID = connection.requested_by == userID.lowercased() ? connection.requested_for : connection.requested_by
            connectedUserStatus[connectedID] = connection.status
        }
        
        let users = try await getUsers(ids: Array(connectedUserStatus.keys))
        return users.map({ user in
            User(id: user.id, name: user.name, job: user.job, pfp_url: user.pfp_url, connectionStatus: connectedUserStatus[user.id])
        })
    }
    
    func updateUser(name: String, jobTitle: String) async throws {
        let userID = supabase.auth.currentUser!.id.uuidString
        try await supabase.from("profiles").update(["name": name, "job": jobTitle]).eq("id", value: userID).execute()
    }
    
    func uploadPfp(fileName: String, imageData: Data) async throws {
        let userID = supabase.auth.currentUser!.id.uuidString
        try await supabase.storage.from("pfp").upload(fileName, data: imageData)
        
        let url = try supabase.storage.from("pfp").getPublicURL(path: fileName)
        try await supabase.from("profiles").update(["pfp_url": url]).eq("id", value: userID).execute()
    }
}
