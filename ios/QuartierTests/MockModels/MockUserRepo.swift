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

//Mock the behavior of the profiles table in the supabase db

final class MockUserRepo: UserRepository, @unchecked Sendable {
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
    
    func getUser() async throws -> Quartier.User { //Return user's profile
        return users[0]
    }
    
    func getUsers(ids: [String]) async throws -> [Quartier.User] { //Get a list of users from ids
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
        let path = imageData.base64EncodedString(options: .endLineWithLineFeed) //Decode the fake image (containing a string for the path)
        users[0] = User(id: "0", name: users[0].name, job: users[0].job, pfp_url: path)
    }
}
