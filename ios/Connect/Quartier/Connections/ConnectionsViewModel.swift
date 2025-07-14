//
//  ConnectionsViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import Foundation

@MainActor
class ConnectionsViewModel : ObservableObject {
    private var userDatabase = UserDatabase.shared
    private var connectionsDatabase = ConnectionsDatabase.shared
    private var socialsDatabase = SocialsDatabase.shared
    
    @Published var connections: [User] = []
    @Published var requests: [User] = []
    @Published var socials: [String: Socials] = [:]
    
    func refreshConnections() {
        Task {
            let connectionsArray = try await connectionsDatabase.getConnections()
            let connectedUsers = try await userDatabase.getConnectedUsers(connections: connectionsArray)
            requests = connectedUsers.filter { $0.connectionStatus == "pending" }
            connections = connectedUsers.filter { $0.connectionStatus == "accepted" }
        }
        Task {
            let connectedSocials = try await socialsDatabase.getConnectedSocials()
            socials = connectedSocials.reduce(into: [String: Socials]()) { result, userSocials in
                result[userSocials.id] = userSocials
            }
        }
    }
    
    func acceptConnection(uid: String) {
        Task {
            try await connectionsDatabase.acceptConnection(requestedID: uid)
            refreshConnections()
        }
    }
    
    func declineConnection(uid: String) {
        Task {
            try await connectionsDatabase.deleteConnection(requestedID: uid)
            refreshConnections()
        }
    }
}
