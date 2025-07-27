//
//  ConnectVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
@testable import Quartier

@MainActor
struct ConnectionsVMTests {
    @Test func refreshConnections() async throws {
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo)
        
        let _ = viewModel.$connections.dropFirst().sink { users in
            #expect(users == [
                User(id: "0", name: "name", job: "job", connectionStatus: "accepted"),
                User(id: "1", name: "name", job: "job", connectionStatus: "accepted"),
            ])
        }
        
        let _ = viewModel.$requests.dropFirst().sink { users in
            #expect(users == [
                User(id: "2", name: "name", job: "job", connectionStatus: "pending"),
                User(id: "3", name: "name", job: "job", connectionStatus: "pending"),
            ])
        }
        
        let _ = viewModel.$socials.dropFirst().sink { socials in
            #expect(socials == [
                "2": Socials(id: "2", linkedin_url: "link")
            ])
        }
        
        viewModel.refreshConnections()
    }
    
    @Test func acceptConnection() async throws {
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo)
        
        let _ = viewModel.$connections.dropFirst().sink { users in
            #expect(users.contains(where: { $0.id == "4" && $0.connectionStatus == "accepted"}))
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "4", requested_for: "0", status: "accepted")))
        }
        
        viewModel.acceptConnection(uid: "4")
    }
    
    @Test func deleteConnection() async throws {
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo)
        
        let _ = viewModel.$connections.dropFirst().sink { users in
            #expect(!users.contains(where: { $0.id == "4" }))
            let connections = connectionsRepo.connections
            #expect(!connections.contains(where: {$0.requested_by == "4" && $0.requested_for == "0"}))
        }
        
        viewModel.declineConnection(uid: "4")
    }
}
