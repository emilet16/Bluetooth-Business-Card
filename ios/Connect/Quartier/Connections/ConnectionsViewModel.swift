//
//  ConnectionsViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import Foundation

@MainActor
class ConnectionsViewModel : ObservableObject {
    private var userRepository: any UserRepository = UserDatabase.shared
    private var connectionsRepository: any ConnectionsRepository = ConnectionsDatabase.shared
    private var socialsRepository: any SocialsRepository = SocialsDatabase.shared
    
    @Published var connections: [User] = []
    @Published var requests: [User] = []
    @Published var socials: [String: Socials] = [:]
    
    func refreshConnections() {
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
