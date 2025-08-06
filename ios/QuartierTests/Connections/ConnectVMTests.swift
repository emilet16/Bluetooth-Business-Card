//
//  ConnectVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
@testable import Quartier

//Test the Connect viewmodel

@MainActor
struct ConnectVMTests {

    @Test func validUserIds() async throws { //Nearby users are all valid ids
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$users.dropFirst().sink { users in
            #expect(viewModel.users == [
                User(id: "1", name: "name", job: "job"),
                User(id: "2", name: "name", job: "job"),
                User(id: "3", name: "name", job: "job"),
            ])
        }
        
        bleCRepo.uids = ["1", "2", "3"]
    }
    
    @Test func emptyUserIds() async throws { //No nearby users
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$users.dropFirst().sink { users in
            #expect(viewModel.users == [])
        }
        
        bleCRepo.uids = []
    }
    
    @Test func invalidUserId() async throws { //One user has an invalid user ID
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$users.dropFirst().sink { users in
            #expect(viewModel.users == [
                User(id: "1", name: "name", job: "job"),
                User(id: "2", name: "name", job: "job"),
            ])
        }
        
        bleCRepo.uids = ["-1", "1", "2"]
    }
    
    @Test func bleOff() async throws { //Notify the user Bluetooth is turned off
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Bluetooth is powered off")
        }
        
        blePRepo.state = .poweredOff
    }
    
    @Test func bleUnauth() async throws { //Notify the user Bluetooth is unauthorized
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == "Bluetooth is unauthorized, please allow it in settings.")
        }
        
        blePRepo.state = .unauthorized
    }
    
    @Test func bleStatus() async throws { //Don't show anything when Bluetooth is working as expected
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == nil)
        }
        
        blePRepo.state = .poweredOn
    }
    
    @Test func connectWithAlreadyConnected_askedBySelf() async throws { //User already connected
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "You are already connected to this user.")
        }
        
        viewModel.connectWithUser(requestedID: "1")
    }
    
    @Test func connectWithAlreadyConnected_askedByOther() async throws { //User already connected
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "You are already connected to this user.")
        }
        
        viewModel.connectWithUser(requestedID: "2")
    }
    
    @Test func connectWithPending_askedBySelf() async throws { //Connection was already requested, ask the user to wait
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "Connection request pending...")
        }
        
        viewModel.connectWithUser(requestedID: "3")
    }
    
    @Test func connectWithPending_askedByOther() async throws { //Accept the connection request
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "Connection request accepted!")
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "4", requested_for: "0", status: "accepted")))
        }
        
        viewModel.connectWithUser(requestedID: "4")
    }
    
    @Test func connectWithSelf() async throws { //Prevent the user from connecting with themselves
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "You cannot connect with yourself!")
        }
        
        viewModel.connectWithUser(requestedID: "0")
    }
    
    @Test func connectWithInvalidStatus() async throws { //Invalid connection status, notify the user of the error
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "An error happened, please try again!")
        }
        
        viewModel.connectWithUser(requestedID: "5")
    }
    
    @Test func connectWithNewUser() async throws { //Send a connection request
        let blePRepo = MockBlePeripheralManager()
        let bleCRepo = MockBleCentralManager()
        let userRepo = MockUserRepo()
        let connectionsRepo = MockConnectionsRepo()
        let viewModel = ConnectViewModelImpl(blePeripheralManager: blePRepo, bleCentralManager: bleCRepo, userRepository: userRepo, connectionsRepository: connectionsRepo)
        
        let _ = viewModel.$connectionMessage.dropFirst().sink { message in
            #expect(message == "Connection request sent!")
            let connections = connectionsRepo.connections
            #expect(connections.contains(Connection(requested_by: "0", requested_for: "6", status: "pending")))
        }
        
        viewModel.connectWithUser(requestedID: "6")
    }
}
