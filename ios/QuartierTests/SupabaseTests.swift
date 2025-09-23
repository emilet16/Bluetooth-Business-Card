//
//  SupabaseTests.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-08-08.
//

import Foundation
import Testing
@testable import Quartier

func createUser() async throws -> String { //Create a user for tests and return the uuid
    let auth = AuthManagerImpl.shared
    
    let email = UUID().uuidString+"@example.com"
    
    try await auth.signup(email: email, pwd: "Password123", name: "Test User")
    return email
}

@Suite(.serialized) struct SupabaseTests { //All the supabase tests are in the same class since they need to be run one by one to work
    /*--------------------------------
               AUTH TESTS
    --------------------------------*/
    
    @Test func signup_valid() async throws { //Try creating a new user
        let auth = AuthManagerImpl.shared
        
        await #expect(throws: Never.self) {
            try await auth.signup(email: UUID().uuidString+"@example.com", pwd: "Password123", name: "Test User")
        }
    }
    
    @Test func signin_valid() async throws { //Create a new user and then login
        let auth = AuthManagerImpl.shared
        
        let email = try await createUser()
        
        await #expect(throws: Never.self) {
            try await auth.login(email: email, pwd: "Password123")
        }
    }
    
    /*--------------------------------
            USER TABLE TESTS
    --------------------------------*/
    
    @Test func getUser_profile() async throws { //Try getting a user's profile
        let db = UserDatabase.shared
        
        let _ = try await createUser() //Create a random user and get their profile
        let user = try await db.getUser()
        #expect(user!.name == "Test User") //By default, the name should be "Test User" (see AuthTests for the createUser() function)
    }
    
    @Test func getUsers() async throws { //Create 2 users, and get their profiles by user id
        let db = UserDatabase.shared
        
        let _ = try await createUser() //Create user 1
        let user1 = try await db.getUser()
        
        let _ = try await createUser() //Create user 2
        let user2 = try await db.getUser()
        
        let users = try await db.getUsers(ids: [user1!.id, user2!.id])
        #expect(users.contains([user1!, user2!]))
    }
    
    @Test func updateUserProfile() async throws { //Edit a user's profile
        let db = UserDatabase.shared
        
        let _ = try await createUser()
    
        try await db.updateUser(name: "New Name", jobTitle: "Amazing Job Title") //Change their profile
        let newUser = try await db.getUser() //Get the profile to ensure the changes have been applied
        
        #expect(newUser!.name == "New Name")
        #expect(newUser!.job == "Amazing Job Title")
    }
    
    @Test func uploadUserImage() async throws {
        let db = UserDatabase.shared
        
        let _ = try await createUser()
        
        let imageUrl = "https://picsum.photos/400/400.jpg" //Placeholder image
        let imageData = try Data(contentsOf: URL(string: imageUrl)!)
        
        try await db.uploadPfp(fileName: UUID().uuidString+".jpg", imageData: imageData) //Upload image
        let profile = try await db.getUser()
        
        #expect(profile!.pfp_url != nil) //Make sure there is a link pointing to the image
    }
    
    /*--------------------------------
         CONNECTIONS TABLE TESTS
    --------------------------------*/
    @Test func requestConnection_andGet() async throws { //Get all of a user's connections
        let userDB = UserDatabase.shared
        let db = ConnectionsDatabase.shared
        
        let _ = try await createUser() //Create a random user and get their id
        let user1ID = try await userDB.getUser()!.id
        
        let _ = try await createUser() //Create another user and get their id
        let user2ID = try await userDB.getUser()!.id
        let _ = try await db.requestConnection(requestedID: user1ID) //Make the connection
        
        let connection = try await db.getConnectionWithUser(requestedID: user1ID) //Check if the connection was made properly
        #expect(connection?.requested_by == user2ID)
        #expect(connection?.requested_for == user1ID)
        #expect(connection?.status == "pending")
    }
    
    @Test func requestConnection_withSelf() async throws { //Get all of a user's connections
        let userDB = UserDatabase.shared
        let db = ConnectionsDatabase.shared
        
        let _ = try await createUser() //Create a random user and get their id
        let user1ID = try await userDB.getUser()!.id
        
        let result = try await db.requestConnection(requestedID: user1ID) //Make the connection
        
        #expect(result == .cannotConnectWithSelf)
    }
    
    @Test func acceptConnection_andGet() async throws {
        let auth = AuthManagerImpl.shared
        let userDB = UserDatabase.shared
        let db = ConnectionsDatabase.shared
        
        let email = try await createUser() //Create a random user and get their email
        let user1ID = try await userDB.getUser()!.id
        
        let _ = try await createUser()
        let user2ID = try await userDB.getUser()!.id //Create another user and get their id
        let _ = try await db.requestConnection(requestedID: user1ID) //Make the connection
        
        try await auth.login(email: email, pwd: "Password123")
        let _ = try await db.acceptConnection(requestedID: user2ID)
        
        let connection = try await db.getConnectionWithUser(requestedID: user2ID) //Check if the connection was made properly
        #expect(connection?.requested_by == user2ID)
        #expect(connection?.requested_for == user1ID)
        #expect(connection?.status == "accepted")
    }
    
    @Test func deleteConnection_andGet() async throws {
        let auth = AuthManagerImpl.shared
        let userDB = UserDatabase.shared
        let db = ConnectionsDatabase.shared
        
        let email = try await createUser() //Create a random user and get their email
        let user1ID = try await userDB.getUser()!.id
        
        let _ = try await createUser()
        let user2ID = try await userDB.getUser()!.id //Create another user and get their id
        let _ = try await db.requestConnection(requestedID: user1ID) //Make the connection
        
        try await auth.login(email: email, pwd: "Password123")
        let _ = try await db.deleteConnection(requestedID: user2ID)
        
        let connection = try await db.getConnectionWithUser(requestedID: user2ID) //Check if the connection was made properly
        #expect(connection == nil)
    }
    
    @Test func getConnections() async throws {
        let auth = AuthManagerImpl.shared
        let userDB = UserDatabase.shared
        let db = ConnectionsDatabase.shared
        
        let email = try await createUser() //Create a random user and get their email
        let user1ID = try await userDB.getUser()!.id
        
        let _ = try await createUser()
        let user2ID = try await userDB.getUser()!.id //Create another user and get their id
        let _ = try await db.requestConnection(requestedID: user1ID) //Make the connection
        
        let _ = try await createUser()
        let user3ID = try await userDB.getUser()!.id //Create another user and get their id
        let _ = try await db.requestConnection(requestedID: user1ID) //Make the connection
        
        try await auth.login(email: email, pwd: "Password123")
        let connections = try await db.getConnections()
        #expect(connections.contains(Connection(requested_by: user2ID, requested_for: user1ID, status: "pending")))
        #expect(connections.contains(Connection(requested_by: user3ID, requested_for: user1ID, status: "pending")))
    }
    
    /*--------------------------------
           SOCIALS TABLE TESTS
    --------------------------------*/
    @Test func upsert_and_getSocials() async throws {
        let db = SocialsDatabase.shared
        
        let _ = try await createUser()
        try await db.upsertSocials(linkedInUrl: "https://www.linkedin.com/in/user")
        
        let socials = try await db.getUserSocials()
        #expect(socials!.linkedin_url == "https://www.linkedin.com/in/user")
    }
    
    @Test func getConnectedSocials() async throws {
        let auth = AuthManagerImpl.shared
        let userDB = UserDatabase.shared
        let connDB = ConnectionsDatabase.shared
        let socialsDB = SocialsDatabase.shared
        
        let email = try await createUser() //Create a random user and get their email
        let user1ID = try await userDB.getUser()!.id
        
        let _ = try await createUser()
        let user2ID = try await userDB.getUser()!.id //Create another user and get their id
        try await socialsDB.upsertSocials(linkedInUrl: "https://www.linkedin.com/in/user2")
        let _ = try await connDB.requestConnection(requestedID: user1ID) //Make the connection
        
        let _ = try await createUser()
        let user3ID = try await userDB.getUser()!.id //Create another user and get their id
        try await socialsDB.upsertSocials(linkedInUrl: "https://www.linkedin.com/in/user3")
        let _ = try await connDB.requestConnection(requestedID: user1ID) //Make the connection
        
        try await auth.login(email: email, pwd: "Password123")
        let _ = try await connDB.acceptConnection(requestedID: user2ID)
        let _ = try await connDB.acceptConnection(requestedID: user3ID)
        
        let socials = try await socialsDB.getConnectedSocials()
        #expect(socials.contains([
            Socials(id: user2ID, linkedin_url: "https://www.linkedin.com/in/user2"),
            Socials(id: user3ID, linkedin_url: "https://www.linkedin.com/in/user3"),
        ]))
    }
}
