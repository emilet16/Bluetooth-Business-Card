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

struct Connection: Codable, Sendable, Hashable {
    var requested_by: String
    var requested_for: String
    var status: String
}

struct Socials: Decodable, Hashable {
    var id: String
    var linkedin_url: String?
}

class UserRepository {
    static let shared = UserRepository()
    
    func getUser(id: String) async throws -> User {
        return try await (supabase.from("profiles").select("*").eq("id", value: id).execute().value as [User]).first!
    }
    
    func getUsers(ids: [String]) async throws -> [User] {
        return try await supabase.from("profiles").select("*").in("id", values: ids).execute().value
    }
    
    private func getConnections(userID: String) async throws -> [Connection] {
        return try await supabase.from("connections").select("*").or("and(requested_by.eq.\(userID),status.eq.accepted),requested_for.eq.\(userID)").execute().value as [Connection]
    }
    
    func getConnectedUsers(userID: String) async throws -> [User] {
        let connections = try await getConnections(userID: userID)
        
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
    
    func requestConnection(userID: String, requestedID: String) async throws {
        var currentConnection: Connection? = nil
        
        currentConnection = try await (supabase.from("connections").select("*").or("and(requested_for.eq.\(userID), requested_by.eq.\(requestedID)), and(requested_by.eq.\(userID), requested_for.eq.\(requestedID))").execute().value as [Connection]).first
        if(currentConnection == nil) {
            try await supabase.from("connections").upsert(Connection(requested_by: userID, requested_for: requestedID, status: "pending")).execute()
        } else if(currentConnection?.status == "accepted") {
            //TODO
        } else if (currentConnection?.status == "pending" && currentConnection?.requested_by == requestedID) {
            try await acceptConnection(userID: userID, requestedID: requestedID)
        }
        else {
            print("test")
        }
    }
    
    func acceptConnection(userID: String, requestedID: String) async throws {
        try await supabase.from("connections").update(["status":"accepted"]).eq("requested_by", value: requestedID).eq("requested_for", value: userID).execute()
    }
    
    func deleteConnection(userID: String, requestedID: String) async throws {
        try await supabase.from("connections").delete().eq("requested_by", value: requestedID).eq("requested_for", value: userID).execute()
    }
    
    func getUserSocials(id: String) async throws -> Socials {
        return try await (supabase.from("socials").select("*").eq("id", value: id).execute().value as [Socials]).first!
    }
    
    func getConnectedSocials() async throws -> [Socials] {
        return try await supabase.from("socials").select("*").execute().value as [Socials] //returns socials of all connected people
    }
    
    func updateUser(userID: String, name: String, jobTitle: String) async throws {
        try await supabase.from("profiles").update(["name": name, "job": jobTitle]).eq("id", value: userID).execute()
    }
    
    func upsertSocials(userID: String, linkedInUrl: String) async throws {
        try await supabase.from("socials").upsert(["id": userID, "linkedin_url": linkedInUrl], onConflict: "id").execute()
    }
    
    func uploadPfp(userID: String, fileName: String, imageData: Data) async throws {
        try await supabase.storage.from("pfp").upload(fileName, data: imageData)
        
        let url = try supabase.storage.from("pfp").getPublicURL(path: fileName)
        try await supabase.from("profiles").update(["pfp_url": url]).eq("id", value: userID).execute()
    }
}
