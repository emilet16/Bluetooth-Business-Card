//
//  ConnectionsView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct ConnectionsView<T: ConnectionsViewModel> : View {
    @Environment(\.openURL) private var openURL
    @StateObject var viewModel: T
    
    var body : some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                Text("Connection Requests").font(.robotoBold(17)).frame(maxWidth: .infinity, alignment: .leading)
                LazyHStack {
                    if(viewModel.requests.isEmpty) {
                        Text("You have no connection requests").font(.roboto(17))
                    } else {
                        ForEach(viewModel.requests, id: \.self) { request in
                            ConnectionRequest(user: request, onAccept: { uid in
                                viewModel.acceptConnection(uid: uid)
                            }, onDecline: { uid in
                                viewModel.declineConnection(uid: uid)
                            })
                        }
                    }
                }.frame(maxWidth: .infinity)
                Text("Connections").font(.robotoBold(17)).frame(maxWidth: .infinity, alignment: .leading)
                if(viewModel.connections.isEmpty) {
                    Text("You have no connections").font(.roboto(17))
                } else {
                    ForEach(viewModel.connections, id: \.self) { connection in
                        UserCard(user: connection, socials: viewModel.socials[connection.id], onViewProfile: { url in
                            if(url != nil) {
                                if let url = URL(string: url!) {
                                    openURL(url)
                                }
                            }
                        })
                    }
                }
            }.padding()
        }.toolbar {
            ToolbarItem(placement: .principal) {
                Text("Connections").font(.poppins(24))
            }
            ToolbarItem(placement: .primaryAction) {
                NavigationLink (destination: ConnectView(viewModel: ConnectViewModelImpl())) {
                    Image(systemName: "plus.circle")
                }
            }
            
        }
        .refreshable {
            viewModel.refreshConnections()
        }
        .onAppear(){
            viewModel.refreshConnections()
        }
    }
}

#Preview {
    ConnectionsView(viewModel: MockConnnectionsVM())
}

class MockConnnectionsVM : ConnectionsViewModel {
    var connections: [User] = [
        User(id: "0", name: "Steve Jobs", job: "CEO"),
        User(id: "1", name: "Bill Gates", job: "Founder")
    ]
    var requests: [User] = [
        User(id: "2", name: "Mark Zuckerberg", job: "Dropout"),
        User(id: "3", name: "Larry Page", job: "Co-Founder")
    ]
    var socials: [String: Socials] = [:]
    
    func refreshConnections() {}
    func acceptConnection(uid: String) {}
    func declineConnection(uid: String) {}
}
