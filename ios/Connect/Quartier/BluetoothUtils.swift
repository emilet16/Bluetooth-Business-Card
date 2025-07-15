//
//  BluetoothUtils.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-18.
//

import Foundation
import CoreBluetooth
import os

class BluetoothPeripheralManager: NSObject, ObservableObject, CBPeripheralManagerDelegate {
    private var peripheralManager: CBPeripheralManager?
    private var shouldAdvertise: Bool = false
    
    @Published var status: CBManagerState? = nil
    
    override init() {
        super.init()
        self.peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
        self.status = peripheralManager?.state
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        status = peripheral.state
        switch peripheral.state {
       case .poweredOn:
           print("Peripheral Manager is powered on.")
           if shouldAdvertise {
               startAdvertising()
           }
       case .poweredOff:
           print("Bluetooth is powered off.")
           stopAdvertising()
       case .unauthorized:
           print("Bluetooth not authorized.")
       case .unsupported:
           print("Bluetooth unsupported.")
       case .resetting:
           print("Bluetooth resetting.")
           stopAdvertising()
       case .unknown:
           print("Bluetooth state unknown.")
       @unknown default:
           print("Unknown peripheral manager state.")
       }
    }
    
    func startAdvertising() {
        switch(peripheralManager?.state) {
        case .poweredOn:
            let serviceUUID = CBUUID(string: "D17B")
            let userUUID = CBUUID(string: supabase.auth.currentUser!.id.uuidString)
            
            peripheralManager?.startAdvertising([
                CBAdvertisementDataServiceUUIDsKey: [serviceUUID, userUUID],
            ])
            shouldAdvertise = false
        default:
            shouldAdvertise = true
        }
    }
    
    func stopAdvertising() {
        peripheralManager?.stopAdvertising()
        shouldAdvertise = false
    }
}

class BluetoothCentralManager: NSObject, ObservableObject, CBCentralManagerDelegate {
    @Published var discoveredUIDS: [String] = []
    
    private var centralManager: CBCentralManager!
    private let targetServiceUUID = CBUUID(string: "D17B")
    
    private var shouldScan = false
    
    override init() {
        super.init()
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            print("Bluetooth is powered on.")
            if shouldScan {
                startScan()
            }
        case .poweredOff:
            print("Bluetooth is powered off.")
            stopScan()
        case .unauthorized:
            print("Bluetooth not authorized.")
        case .unsupported:
            print("Bluetooth unsupported.")
        case .resetting:
            print("Bluetooth resetting.")
            stopScan()
        case .unknown:
            print("Bluetooth state unknown.")
        @unknown default:
            print("Unknown central manager state.")
        }
    }
    
    func startScan() {
        switch(centralManager.state) {
        case .poweredOn:
            discoveredUIDS = []
            centralManager.scanForPeripherals(withServices: [targetServiceUUID], options: [
                CBCentralManagerScanOptionAllowDuplicatesKey: false
            ])
            shouldScan = false
        default:
            shouldScan = true
        }
    }
    
    func stopScan() {
        centralManager.stopScan()
        shouldScan = false
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        var services = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID] ?? []
        services.removeAll(where: { $0 == targetServiceUUID})
        let uid = services.first?.uuidString
        if(uid != nil) {
            if(!discoveredUIDS.contains(where: {$0 == uid})) {
                discoveredUIDS.append(uid!)
            }
        }
    }
}
