//
//  ConnectView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import SwiftUI

struct ConnectView : View {
    @StateObject var blePeripheralManager = BluetoothPeripheralManager()
    @StateObject var bleCentralManager = BluetoothCentralManager()
    
    let columns = [GridItem(.flexible()), GridItem(.flexible())]
    
    var scanTask: Task<Void, Never>? = nil
    
    var body : some View {
        ScrollView {
            LazyVGrid(columns: columns) {
                ForEach(bleCentralManager.discoveredUIDS, id: \.self) { value in
                    Button(action: {
                        
                    }) {
                        VStack {
                            
                        }
                    }
                    .padding()
                }
            }
        }
        .onAppear() {
            blePeripheralManager.userID = supabase.auth.currentUser?.id.uuidString
            blePeripheralManager.startAdvertising()
            bleCentralManager.startScan()
            scanTask?.cancel()
            scanTask = Task {
                try await Task.sleep(for: .seconds(10))
                bleCentralManager.stopScan()
            }
        }
        .onDisappear() {
            blePeripheralManager.stopAdvertising()
            bleCentralManager.stopScan()
        }
        .toolbar() {
            Button("Refresh") {
                bleCentralManager.startScan()
                scanTask?.cancel()
                scanTask = Task {
                    try await Task.sleep(for: .seconds(10))
                    bleCentralManager.stopScan()
                }
            }
        }
    }
}
 
