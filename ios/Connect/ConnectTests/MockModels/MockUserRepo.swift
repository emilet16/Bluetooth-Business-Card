//
//  MockUserRepo.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Foundation
import Supabase
import Testing
@testable import Quartier

class MockUserRepo: UserRepository {
    var profileError = false
    var pfpError = false
    
    private(set) var users = [
        User(id: "0", name: "name", job: "job"),
        User(id: "1", name: "name", job: "job"),
        User(id: "2", name: "name", job: "job"),
        User(id: "3", name: "name", job: "job"),
        User(id: "4", name: "name", job: "job"),
        User(id: "5", name: "name", job: "job"),
    ]
    
    func getUser() async throws -> Quartier.User {
        return users[0]
    }
    
    func getUsers(ids: [String]) async throws -> [Quartier.User] {
        users.filter { ids.contains($0.id) }
    }
    
    func updateUser(name: String, jobTitle: String) async throws {
        if(profileError) {
            throw PostgrestError(message: "Error!")
        }
        users[0] = User(id: "0", name: name, job: jobTitle, pfp_url: users[0].pfp_url)
    }
    
    func uploadPfp(fileName: String, imageData: Data) async throws {
        if(profileError) {
            throw PostgrestError(message: "Error!")
        }
        let path = imageData.base64EncodedString(options: .endLineWithLineFeed)
        users[0] = User(id: "0", name: users[0].name, job: users[0].job, pfp_url: path)
    }
}
