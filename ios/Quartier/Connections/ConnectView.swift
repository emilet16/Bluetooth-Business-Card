//
//  ConnectView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import SwiftUI

//Screen to connect with other users

struct ConnectView<T: ConnectViewModel> : View {
    @StateObject var viewModel: T
    
    var body : some View {
        ZStack(alignment: .bottom) {
            //Show a message to the user about the connection status (ex. connection request sent)
            if let connectionMessage = viewModel.connectionMessage {
                Text(connectionMessage).font(.body(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            ScrollView {
                if let message = viewModel.message { //Show a message if an error happens (ex. Bluetooth not available)
                    Text(message)
                } else if(viewModel.users.isEmpty) {
                    Text("No nearby people").font(.body(17))
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
        }
        .toolbar() {
            ToolbarItem(placement: .principal) {
                Text("Nearby People").font(.title(24))
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

#Preview {
    ConnectView(viewModel: MockConnectVM())
}

class MockConnectVM : ConnectViewModel {
    var users: [User] = [
        User(id: "0", name: "Steve Jobs", job: "CEO"),
        User(id: "1", name: "Bill Gates", job: "Founder")
    ]
    var message: String? = nil
    var connectionMessage: String? = nil
    
    func connectWithUser(requestedID: String) {}
    func startAdvertising() {}
    func stopAdvertising() {}
    func startScan() {}
    func stopScan() {}
    func updateBleState() {}
    func updateUsers() {}
}
