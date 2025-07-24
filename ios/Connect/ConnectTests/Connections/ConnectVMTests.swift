//
//  ConnectVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
@testable import Quartier

@MainActor
struct ConnectVMTests {

    @Test func validUserIds() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$users.dropFirst().sink { users in
            #expect(viewModel.users == [
                User(id: "1", name: "name", job: "job"),
                User(id: "2", name: "name", job: "job"),
                User(id: "3", name: "name", job: "job"),
            ])
        }
        
        bleCRepo.uids = ["1", "2", "3"]
    }
    
    @Test func emptyUserIds() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$users.dropFirst().sink { users in
            #expect(viewModel.users == [])
        }
        
        bleCRepo.uids = []
    }
    
    @Test func invalidUserId() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$users.dropFirst().sink { users in
            #expect(viewModel.users == [
                User(id: "1", name: "name", job: "job"),
                User(id: "2", name: "name", job: "job"),
            ])
        }
        
        bleCRepo.uids = ["-1", "1", "2"]
    }
    
    @Test func bleOff() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Bluetooth is powered off")
        }
        
        blePRepo.state = .poweredOff
    }
    
    @Test func bleUnauth() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Bluetooth is unauthorized, please allow it in settings.")
        }
        
        blePRepo.state = .unauthorized
    }
    
    @Test func bleStatus() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == nil)
        }
        
        blePRepo.state = .poweredOn
    }
    
    @Test func connectWithAlreadyConnected_askedBySelf() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "You are already connected to this user.")
        }
        
        viewModel.connectWithUser(requestedID: "1")
    }
    
    @Test func connectWithAlreadyConnected_askedByOther() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "You are already connected to this user.")
        }
        
        viewModel.connectWithUser(requestedID: "2")
    }
    
    @Test func connectWithPending_askedBySelf() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "Connection request pending...")
        }
        
        viewModel.connectWithUser(requestedID: "3")
    }
    
    @Test func connectWithPending_askedByOther() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "Connection request accepted!")
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "4", requested_for: "0", status: "accepted")))
        }
        
        viewModel.connectWithUser(requestedID: "4")
    }
    
    @Test func connectWithSelf() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "You cannot connect with yourself!")
        }
        
        viewModel.connectWithUser(requestedID: "0")
    }
    
    @Test func connectWithInvalidStatus() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "An error happened, please try again!")
        }
        
        viewModel.connectWithUser(requestedID: "5")
    }
    
    @Test func connectWithNewUser() async throws {
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModel(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "Connection request sent!")
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "0", requested_for: "6", status: "pending")))
        }
        
        viewModel.connectWithUser(requestedID: "6")
    }
}
