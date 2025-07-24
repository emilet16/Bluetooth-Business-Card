//
//  MockBleRepo.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Foundation
import CoreBluetooth
import Combine
@testable import Quartier

class MockBlePeripheralManager : NSObject, BluetoothPeripheralManager {
    @Published var state: CBManagerState? = nil
    var status: AnyPublisher<CBManagerState?, Never> {
        $state.eraseToAnyPublisher()
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {}
    func startAdvertising() {}
    func stopAdvertising() {}
}

class MockBleCentralManager : NSObject, BluetoothCentralManager {
    @Published var uids: [String] = []
    var discoveredUIDS: AnyPublisher<[String], Never> {
        $uids.eraseToAnyPublisher()
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {}
    func startScan() {}
    func stopScan() {}
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {}
}
