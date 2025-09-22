//
//  ConnectionsViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import Foundation

//A view model for the connections screen, sorts the connected users and handles connection requests
@MainActor
protocol ConnectionsViewModel : ObservableObject {
    var connections: [User] { get }
    var requests: [User] { get }
    var socials: [String: Socials] { get }
    
    func refreshConnections()
    func acceptConnection(uid: String)
    func declineConnection(uid: String)
}

class ConnectionsViewModelImpl : ConnectionsViewModel {
    private var userRepository: any UserRepository
    private var connectionsRepository: any ConnectionsRepository
    private var socialsRepository: any SocialsRepository
    
    @Published var connections: [User] = []
    @Published var requests: [User] = []
    @Published var socials: [String: Socials] = [:]
    
    init(userRepository: any UserRepository = UserDatabase.shared, connectionsRepository: any ConnectionsRepository = ConnectionsDatabase.shared, socialsRepository: any SocialsRepository = SocialsDatabase.shared) {
        self.userRepository = userRepository
        self.connectionsRepository = connectionsRepository
        self.socialsRepository = socialsRepository
    }
    
    func refreshConnections() { //Load connections and sort them to display
        Task {
            let connectionsArray = try await connectionsRepository.getConnections()
            
            let connectedUsers = try await connectionsToUsers(connections: connectionsArray)
            
            requests = connectedUsers.filter { $0.connectionStatus == "pending" }
            connections = connectedUsers.filter { $0.connectionStatus == "accepted" }
        }
        Task {
            let connectedSocials = try await socialsRepository.getConnectedSocials()
            socials = connectedSocials.reduce(into: [String: Socials]()) { result, userSocials in
                result[userSocials.id] = userSocials
            }
        }
    }
    
    //Get connected users profiles
    private func connectionsToUsers(connections: [Connection]) async throws -> [User] {
        let userID = supabase.auth.currentUser!.id.uuidString
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
    
    func acceptConnection(uid: String) {
        Task {
            try await connectionsRepository.acceptConnection(requestedID: uid)
            refreshConnections()
        }
    }
    
    func declineConnection(uid: String) {
        Task {
            try await connectionsRepository.deleteConnection(requestedID: uid)
            refreshConnections()
        }
    }
}
