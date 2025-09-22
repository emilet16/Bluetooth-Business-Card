//
//  ConnectViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation
import Combine

//Viewmodel class for the connect screen, fetch nearby users and send requests
@MainActor
protocol ConnectViewModel : ObservableObject {
    var users: [User] { get }
    var message: String? { get }
    var connectionMessage: String? { get }
    
    func connectWithUser(requestedID: String)
    func startAdvertising()
    func stopAdvertising()
    func startScan()
    func stopScan()
    func updateBleState()
    func updateUsers()
}

class ConnectViewModelImpl : ConnectViewModel {
    private var blePeripheralManager: any BluetoothPeripheralManager
    private var bleCentralManager: any BluetoothCentralManager
    private var userRepository: any UserRepository
    private var connectionsRepository: any ConnectionsRepository
    
    @Published var users: [User] = []
    @Published var message: String? = nil
    @Published var connectionMessage: String? = nil
    
    private var cancellables = Set<AnyCancellable>()
    private var scanTask: Task<Void, Never>? = nil
    
    init(blePeripheralManager: any BluetoothPeripheralManager = BluetoothPeripheralManagerImpl(), bleCentralManager: any BluetoothCentralManager = BluetoothCentralManagerImpl(),
         userRepository: any UserRepository = UserDatabase.shared, connectionsRepository: any ConnectionsRepository = ConnectionsDatabase.shared) {
        self.blePeripheralManager = blePeripheralManager
        self.bleCentralManager = bleCentralManager
        self.userRepository = userRepository
        self.connectionsRepository = connectionsRepository
        updateBleState()
        updateUsers()
    }
    
    func connectWithUser(requestedID: String) {
        Task {
            let connection = try await connectionsRepository.getConnectionWithUser(requestedID: requestedID)
            
            if(connection == nil) {
                //No connection exist with this user, make sure the user isn't trying to connect with themselves, then send request
                let result = try await connectionsRepository.requestConnection(requestedID: requestedID)
                if(result == .cannotConnectWithSelf) { connectionMessage = "You cannot connect with yourself!" }
                else { connectionMessage = "Connection request sent!" }
            } else if(connection?.status == "pending" && connection?.requested_by == requestedID) {
                //Connection request was sent by the other user, accept it
                try await connectionsRepository.acceptConnection(requestedID: requestedID)
                connectionMessage = "Connection request accepted!"
            } else if(connection?.status == "accepted") {
                //A connection already exists between the users
                connectionMessage = "You are already connected to this user."
            } else if(connection?.status == "pending" && connection?.requested_for == requestedID) {
                //Connection request was already sent, wait for confirmation on the other side
                connectionMessage = "Connection request pending..."
            } else {
                //Show the user if something unexpected happens
                connectionMessage = "An error happened, please try again!"
            }
            
            try await Task.sleep(for: .seconds(2))
            connectionMessage = nil
        }
    }
    
    func startAdvertising() {
        blePeripheralManager.startAdvertising()
    }
    
    func stopAdvertising() {
        blePeripheralManager.stopAdvertising()
    }
    
    func startScan() {
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
    
    func stopScan() {
        bleCentralManager.stopScan()
        scanTask?.cancel()
    }
    
    func updateBleState() {
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
    
    func updateUsers() {
        //Fetch the nearby users' profiles
        bleCentralManager.discoveredUIDS
            .sink { [weak self] uids in
                Task {
                    self?.users = try await self?.userRepository.getUsers(ids: uids) ?? []
                }
        }.store(in: &cancellables)
    }
}
