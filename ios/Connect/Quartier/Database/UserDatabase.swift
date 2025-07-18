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

protocol UserRepository {
    func getUser() async throws -> User
    func getUsers(ids: [String]) async throws -> [User]
    func updateUser(name: String, jobTitle: String) async throws
    func uploadPfp(fileName: String, imageData: Data) async throws
}

class UserDatabase : UserRepository {
    static let shared = UserDatabase()
    
    func getUser() async throws -> User {
        let id = supabase.auth.currentUser!.id.uuidString
        return try await (supabase.from("profiles").select("*").eq("id", value: id).execute().value as [User]).first!
    }
    
    func getUsers(ids: [String]) async throws -> [User] {
        return try await supabase.from("profiles").select("*").in("id", values: ids).execute().value
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
