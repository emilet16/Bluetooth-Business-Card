//
//  BluetoothUtils.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-18.
//

import Foundation
import CoreBluetooth
import os
import Combine

//Bluetooth peripheral manager, advertises user id to the nearby devices
protocol BluetoothPeripheralManager: ObservableObject, CBPeripheralManagerDelegate {
    var status: AnyPublisher<CBManagerState?, Never> { get }
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager)
    func startAdvertising()
    func stopAdvertising()
}

class BluetoothPeripheralManagerImpl: NSObject, BluetoothPeripheralManager  {
    private var peripheralManager: CBPeripheralManager?
    private var shouldAdvertise: Bool = false
    
    @Published private(set) var state: CBManagerState? = nil
    var status: AnyPublisher<CBManagerState?, Never> {
        $state.eraseToAnyPublisher()
    }
    
    override init() {
        super.init()
        self.peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
        self.state = peripheralManager?.state
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        state = peripheral.state
        switch peripheral.state {
       case .poweredOn:
           print("Peripheral Manager is powered on.")
           if shouldAdvertise { //Start advertising if it was delayed
               startAdvertising()
           }
        case .poweredOff:
            print("Bluetooth is powered off.")
            stopAdvertising()
            shouldAdvertise = true //Restart advertising when Bluetooth is turned back on
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
        shouldAdvertise = true
        if (peripheralManager?.state == .poweredOn) {
            let serviceUUID = CBUUID(string: "D17B")
            let userUUID = CBUUID(string: supabase.auth.currentUser!.id.uuidString)
            
            peripheralManager?.startAdvertising([
                CBAdvertisementDataServiceUUIDsKey: [serviceUUID, userUUID], //Advertise both service uuid and user ID as a workaround for not sending service key data
            ])
        }
    }
    
    func stopAdvertising() {
        peripheralManager?.stopAdvertising()
        shouldAdvertise = false
    }
}

//Bluetooth peripheral manager, scans for nearby users

protocol BluetoothCentralManager : ObservableObject, CBCentralManagerDelegate {
    var discoveredUIDS: AnyPublisher<[String], Never> { get }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager)
    func startScan()
    func stopScan()
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber)
}

class BluetoothCentralManagerImpl: NSObject, BluetoothCentralManager {
    @Published private(set) var uids: [String] = []
    var discoveredUIDS: AnyPublisher<[String], Never> {
        $uids.eraseToAnyPublisher()
    }
    
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
            if shouldScan { //Restart scanning if it was stopped by Bluetooth being turned off
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
        if(centralManager.state == .poweredOn) {
            uids = []
            centralManager.scanForPeripherals(withServices: [targetServiceUUID], options: [ //Filter using withServices, to remove unwanted devices
                CBCentralManagerScanOptionAllowDuplicatesKey: false
            ])
        }
        shouldScan = true
    }
    
    func stopScan() {
        centralManager.stopScan()
        shouldScan = false
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        var services = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID] ?? []
        services.removeAll(where: { $0 == targetServiceUUID}) //Both the userId and the service uuid are being sent in the packet, so remove the service key to get the user ID (the order can vary so indexing doesn't work)
        let uid = services.first?.uuidString
        if(uid != nil) {
            if(!uids.contains(where: {$0 == uid})) {
                uids.append(uid!)
            }
        }
    }
}
