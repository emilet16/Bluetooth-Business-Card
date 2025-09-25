//
//  ConnectVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
@testable import Quartier

//Tests for the Connections viewmodel

@MainActor
struct ConnectionsVMTests {
    @Test func refreshConnections() async throws { //Big test containing multiple test cases defined in MockUserRepo (includes pending, accepted and invalid requests)
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
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
                "1": Socials(id: "1", linkedin_url: nil),
                "2": Socials(id: "2", linkedin_url: "link")
            ])
        }
        
        viewModel.refreshConnections()
    }
    
    @Test func acceptConnection() async throws { //Make sure connection is accepted properly
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$connections.dropFirst().sink { users in
            #expect(users.contains(where: { $0.id == "4" && $0.connectionStatus == "accepted"}))
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "4", requested_for: "0", status: "accepted")))
        }
        
        viewModel.acceptConnection(uid: "4")
    }
    
    @Test func deleteConnection() async throws { //Make sure a connection is deleted properly
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$connections.dropFirst().sink { users in
            #expect(!users.contains(where: { $0.id == "4" }))
            let connections = connectionsRepo.connections
            #expect(!connections.contains(where: {$0.requested_by == "4" && $0.requested_for == "0"}))
        }
        
        viewModel.declineConnection(uid: "4")
    }
    
    @Test func validUserIds() async throws { //Nearby users are all valid ids
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$nearbyUsers.dropFirst().sink { users in
            #expect(users == [
                User(id: "1", name: "name", job: "job"),
                User(id: "2", name: "name", job: "job"),
                User(id: "3", name: "name", job: "job"),
            ])
        }
        
        bleCRepo.uids = ["1", "2", "3"]
    }
    
    @Test func emptyUserIds() async throws { //No nearby users
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$nearbyUsers.dropFirst().sink { users in
            #expect(users == [])
        }
        
        bleCRepo.uids = []
    }
    
    @Test func invalidUserId() async throws { //One user has an invalid user ID
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$nearbyUsers.dropFirst().sink { users in
            #expect(users == [
                User(id: "1", name: "name", job: "job"),
                User(id: "2", name: "name", job: "job"),
            ])
        }
        
        bleCRepo.uids = ["-1", "1", "2"]
    }
    
    @Test func bleOff() async throws { //Notify the user Bluetooth is turned off
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Bluetooth is powered off")
        }
        
        blePRepo.state = .poweredOff
    }
    
    @Test func bleUnauth() async throws { //Notify the user Bluetooth is unauthorized
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Bluetooth is unauthorized, please allow it in settings.")
        }
        
        blePRepo.state = .unauthorized
    }
    
    @Test func bleStatus() async throws { //Don't show anything when Bluetooth is working as expected
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == nil)
        }
        
        blePRepo.state = .poweredOn
    }
    
    @Test func connectWithAlreadyConnected_askedBySelf() async throws { //User already connected
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "You are already connected to this user.")
        }
        
        viewModel.connectWithUser(requestedID: "1")
    }
    
    @Test func connectWithAlreadyConnected_askedByOther() async throws { //User already connected
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "You are already connected to this user.")
        }
        
        viewModel.connectWithUser(requestedID: "2")
    }
    
    @Test func connectWithPending_askedBySelf() async throws { //Connection was already requested, ask the user to wait
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Connection request pending...")
        }
        
        viewModel.connectWithUser(requestedID: "3")
    }
    
    @Test func connectWithPending_askedByOther() async throws { //Accept the connection request
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Connection request accepted!")
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "4", requested_for: "0", status: "accepted")))
        }
        
        viewModel.connectWithUser(requestedID: "4")
    }
    
    @Test func connectWithSelf() async throws { //Prevent the user from connecting with themselves
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "You cannot connect with yourself!")
        }
        
        viewModel.connectWithUser(requestedID: "0")
    }
    
    @Test func connectWithInvalidStatus() async throws { //Invalid connection status, notify the user of the error
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "An error happened, please try again!")
        }
        
        viewModel.connectWithUser(requestedID: "5")
    }
    
    @Test func connectWithNewUser() async throws { //Send a connection request
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let socialsRepo = MockSocialsRepo()
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let viewModel = ConnectionsViewModelImpl(userRepository: userRepo, connectionsRepository: connectionsRepo, socialsRepository: socialsRepo,
            blePeripheralManager: blePRepo, bleCentralManager: bleCRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Connection request sent!")
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "0", requested_for: "6", status: "pending")))
        }
        
        viewModel.connectWithUser(requestedID: "6")
    }
}
