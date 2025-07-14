//
//  ConnectView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import SwiftUI

struct ConnectView : View {
    @StateObject var viewModel = ConnectViewModel()
    
    var body : some View {
        ScrollView {
            if(viewModel.users.isEmpty) {
                Text("No nearby people").font(.roboto(17))
            } else {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())]) {
                    ForEach(viewModel.users, id: \.self) { user in
                        PublicUserCard(user: user, onConnect: {uid in
                            viewModel.connectWithUser(requestedID: uid)
                        })
                    }
                }.frame(maxWidth: .infinity)
            }
        }
        .toolbar() {
            ToolbarItem(placement: .principal) {
                Text("Nearby People").font(.poppins(24))
            }
            ToolbarItem(placement: .primaryAction) {
                Button(action: {
                    viewModel.startScan()
                }) {
                    Image(systemName: "arrow.trianglehead.clockwise.rotate.90")
                }
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
