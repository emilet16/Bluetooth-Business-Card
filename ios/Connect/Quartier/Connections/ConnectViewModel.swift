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
    
    private var cancellables = Set<AnyCancellable>()
    private var scanTask: Task<Void, Never>? = nil
    
    init() {
        updateUsers()
    }
    
    func connectWithUser(requestedID: String) {
        Task {
            try await connectionsDatabase.requestConnection(requestedID: requestedID)
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
    
    func updateUsers() {
        bleCentralManager.$discoveredUIDS
            .sink { [weak self] uids in
                Task {
                    self?.users = try await self?.userDatabase.getUsers(ids: uids) ?? []
                }
        }.store(in: &cancellables)
    }
}
