//
//  ConnectionsViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import Foundation

@MainActor
class ConnectionsViewModel : ObservableObject {
    private var userRepository = UserRepository()
    
    @Published var connections: [User] = []
    @Published var requests: [User] = []
    @Published var socials: [String: Socials] = [:]
    
    private var userID = supabase.auth.currentUser!.id.uuidString
    
    func refreshConnections() {
        Task {
            let connectedUsers = try await userRepository.getConnectedUsers(userID: userID)
            requests = connectedUsers.filter { $0.connectionStatus == "pending" }
            connections = connectedUsers.filter { $0.connectionStatus == "accepted" }
        }
        Task {
            let connectedSocials = try await userRepository.getConnectedSocials()
            socials = connectedSocials.reduce(into: [String: Socials]()) { result, userSocials in
                result[userSocials.id] = userSocials
            }
        }
    }
    
    func acceptConnection(uid: String) {
        Task {
            try await userRepository.acceptConnection(userID: userID, requestedID: uid)
            refreshConnections()
        }
    }
    
    func declineConnection(uid: String) {
        Task {
            try await userRepository.deleteConnection(userID: userID, requestedID: uid)
            refreshConnections()
        }
    }
}
