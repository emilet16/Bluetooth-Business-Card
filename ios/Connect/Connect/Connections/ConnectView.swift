//
//  ConnectView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import SwiftUI

struct ConnectView : View {
    @StateObject var viewModel = ConnectViewModel()
    
    let columns = [GridItem(.flexible()), GridItem(.flexible())]
    
    var body : some View {
        ScrollView {
            LazyVGrid(columns: columns) {
                ForEach(viewModel.users, id: \.self) { user in
                    PublicUserCard(user: user, onConnect: {uid in
                        viewModel.connectWithUser(userID: uid)
                    })
                }
            }
        }
        .toolbar() {
            Button(action: {
                viewModel.startScan()
            }) {
                Image(systemName: "arrow.trianglehead.clockwise.rotate.90")
            }
        }
        .onAppear() {
            viewModel.startAdvertising()
            viewModel.startScan()
            
        }
        .onDisappear() {
            viewModel.stopScan()
            viewModel.stopAdvertising()
        }
    }
}
