//
//  ConnectionsViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import Foundation
import Combine

//A view model for the connections screen, sorts the connected users and handles connection requests
@MainActor
protocol ConnectionsViewModel : ObservableObject {
    var connections: [User] { get }
    var requests: [User] { get }
    var socials: [String: Socials] { get }
    var nearbyUsers: [User] { get }
    var message: String? { get }
    
    func refreshConnections()
    func acceptConnection(uid: String)
    func declineConnection(uid: String)
    func connectWithUser(requestedID: String)
    func initBle()
    func stopBle()

}

class ConnectionsViewModelImpl : ConnectionsViewModel {
    private var userRepository: any UserRepository
    private var connectionsRepository: any ConnectionsRepository
    private var socialsRepository: any SocialsRepository
    private var blePeripheralManager: any BluetoothPeripheralManager
    private var bleCentralManager: any BluetoothCentralManager
    
    @Published var connections: [User] = []
    @Published var requests: [User] = []
    @Published var socials: [String: Socials] = [:]
    @Published var nearbyUsers: [User] = []
    @Published var message: String? = nil
    
    private var cancellables = Set<AnyCancellable>()
    private var scanTask: Task<Void, Never>? = nil
    
    init(userRepository: any UserRepository = UserDatabase.shared, connectionsRepository: any ConnectionsRepository = ConnectionsDatabase.shared,
         socialsRepository: any SocialsRepository = SocialsDatabase.shared, blePeripheralManager: any BluetoothPeripheralManager = BluetoothPeripheralManagerImpl(), bleCentralManager: any BluetoothCentralManager = BluetoothCentralManagerImpl()) {
        self.userRepository = userRepository
        self.connectionsRepository = connectionsRepository
        self.socialsRepository = socialsRepository
        self.blePeripheralManager = blePeripheralManager
        self.bleCentralManager = bleCentralManager
        
        updateBleState()
        updateUsers()
    }
    
    func refreshConnections() { //Load connections and sort them to display
        Task {
            let connectionsArray = try await connectionsRepository.getConnections()
            
            let connectedUsers = try await connectionsRepository.connectionsToUsers(userRepository: userRepository, connections: connectionsArray)
            
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
    
    func connectWithUser(requestedID: String) {
        Task {
            let connection = try await connectionsRepository.getConnectionWithUser(requestedID: requestedID)
            
            if(connection == nil) {
                //No connection exist with this user, make sure the user isn't trying to connect with themselves, then send request
                let result = try await connectionsRepository.requestConnection(requestedID: requestedID)
                if(result == .cannotConnectWithSelf) { message = "You cannot connect with yourself!" }
                else { message = "Connection request sent!" }
            } else if(connection?.status == "pending" && connection?.requested_by == requestedID) {
                //Connection request was sent by the other user, accept it
                try await connectionsRepository.acceptConnection(requestedID: requestedID)
                message = "Connection request accepted!"
            } else if(connection?.status == "accepted") {
                //A connection already exists between the users
                message = "You are already connected to this user."
            } else if(connection?.status == "pending" && connection?.requested_for == requestedID) {
                //Connection request was already sent, wait for confirmation on the other side
                message = "Connection request pending..."
            } else {
                //Show the user if something unexpected happens
                message = "An error happened, please try again!"
            }
            
            hideMessageAfterDelay()
        }
    }
    
    func initBle() {
        startAdvertising()
        startScan()
    }
    
    func stopBle() {
        stopAdvertising()
        stopScan()
    }
    
    private func startAdvertising() {
        blePeripheralManager.startAdvertising()
    }
    
    private func stopAdvertising() {
        blePeripheralManager.stopAdvertising()
    }
    
    private func startScan() {
        bleCentralManager.startScan()
        scanTask?.cancel()
        scanTask = Task { //Stop scanning after 10 seconds for resource optimization
            try? await Task.sleep(for: .seconds(10))
            if (Task.isCancelled) {
                return
            }
            bleCentralManager.stopScan()
            scanTask = nil
            return()
        }
    }
    
    private func stopScan() {
        bleCentralManager.stopScan()
        scanTask?.cancel()
    }
    
    private func updateBleState() {
        //Notify the user if bluetooth is disabled/unauthorized
        blePeripheralManager.status.map({ state in
            switch state {
            case .poweredOff:
                "Bluetooth is powered off"
            case .unauthorized:
                "Bluetooth is unauthorized, please allow it in settings."
            default:
                nil
            }
        }).assign(to: \.message, on: self).store(in: &cancellables)
    }
    
    private func hideMessageAfterDelay() {
        Task {
            try await Task.sleep(for: .seconds(5))
            message = nil
        }
    }
    
    private func updateUsers() {
        //Fetch the nearby users' profiles
        bleCentralManager.discoveredUIDS
            .sink { [weak self] uids in
                Task {
                    self?.nearbyUsers = try await self?.userRepository.getUsers(ids: uids) ?? []
                }
        }.store(in: &cancellables)
    }
}
