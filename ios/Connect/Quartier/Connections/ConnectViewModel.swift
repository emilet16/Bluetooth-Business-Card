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
    private var blePeripheralManager = BluetoothPeripheralManager()
    private var bleCentralManager = BluetoothCentralManager()
    private var userDatabase = UserDatabase.shared
    private var connectionsDatabase = ConnectionsDatabase.shared
    
    @Published var users: [User] = []
    @Published var message: String? = nil
    @Published var connectionMessage: String? = nil
    
    private var cancellables = Set<AnyCancellable>()
    private var scanTask: Task<Void, Never>? = nil
    
    init() {
        updateBleState()
        updateUsers()
    }
    
    func connectWithUser(requestedID: String) {
        Task {
            let result = try await connectionsDatabase.requestConnection(requestedID: requestedID)
            connectionMessage = switch(result) {
                case .accepted: "Connection request accepted!"
                case .alreadyConnected: "You are already connected to this user."
                case .requested: "Connection request sent!"
                case .error: "An error happened, please try again!"
                case .pending: "Connection request pending..."
                default: nil as String?
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
        blePeripheralManager.$status.map({ state in
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
        bleCentralManager.$discoveredUIDS
            .sink { [weak self] uids in
                Task {
                    self?.users = try await self?.userDatabase.getUsers(ids: uids) ?? []
                }
        }.store(in: &cancellables)
    }
}
