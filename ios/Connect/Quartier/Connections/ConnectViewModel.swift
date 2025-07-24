//
//  ConnectViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation
import Combine

@MainActor
class ConnectViewModel : ObservableObject {
    private var blePeripheralManager: any BluetoothPeripheralManager
    private var bleCentralManager: any BluetoothCentralManager
    private var userRepository: any UserRepository
    private var connectionsRepository: any ConnectionsRepository
    
    @Published var users: [User] = []
    @Published var message: String? = nil
    @Published var connectionMessage: String? = nil
    
    private var cancellables = Set<AnyCancellable>()
    private var scanTask: Task<Void, Never>? = nil
    
    init(blePeripheralManager: any BluetoothPeripheralManager = BluetoothPeripheralManagerImpl.shared, bleCentralManager: any BluetoothCentralManager = BluetoothCentralManagerImpl.shared,
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
                let result = try await connectionsRepository.requestConnection(requestedID: requestedID)
                if(result == .cannotConnectWithSelf) { connectionMessage = "You cannot connect with yourself!" }
                else { connectionMessage = "Connection request sent!" }
            } else if(connection?.status == "pending" && connection?.requested_by == requestedID) {
                try await connectionsRepository.acceptConnection(requestedID: requestedID)
                connectionMessage = "Connection request accepted!"
            } else if(connection?.status == "accepted") {
                connectionMessage = "You are already connected to this user."
            } else if(connection?.status == "pending" && connection?.requested_for == requestedID) {
                connectionMessage = "Connection request pending..."
            } else {
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
        scanTask = Task {
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
        bleCentralManager.discoveredUIDS
            .sink { [weak self] uids in
                Task {
                    self?.users = try await self?.userRepository.getUsers(ids: uids) ?? []
                }
        }.store(in: &cancellables)
    }
}
