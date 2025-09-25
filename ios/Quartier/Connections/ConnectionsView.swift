//
//  ConnectionsView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

//A screen showing all connected users

struct ConnectionsView<T: ConnectionsViewModel> : View {
    @Environment(\.openURL) private var openURL
    @StateObject var viewModel: T
    @State var showRequests: Bool = true
    @State var showNearby: Bool = false
    
    var body : some View {
        ZStack(alignment: .bottom) {
            //Show a message to the user about the connection status (ex. connection request sent)
            if let connectionMessage = viewModel.message {
                Text(connectionMessage).font(.body(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            ScrollView {
                LazyVStack(spacing: 10) {
                    HStack(alignment: .center) {
                        Text("Connection Requests").font(.bodyBold(17)).frame(maxWidth: .infinity, alignment: .leading)
                        Button(action: {
                            showRequests = !showRequests
                        }) {
                            if(showRequests) {
                                Image(systemName: "chevron.up")
                            } else {
                                Image(systemName: "chevron.down")
                            }
                        }
                    }.onTapGesture {
                        showRequests = !showRequests
                    }
                    
                    if(showRequests) {
                        LazyHStack {
                            if(viewModel.requests.isEmpty) {
                                Placeholder(title: "No connection requests",
                                            bodyText: "Continue networking!",
                                            iconName: "briefcase")
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
                    }
                    
                    Divider()
                    
                    Text("Connections").font(.bodyBold(17)).frame(maxWidth: .infinity, alignment: .leading)
                    
                    if(viewModel.connections.isEmpty) {
                        Placeholder(title: "Grow your network", bodyText: "Connect with nearby users", iconName: "person",
                                    buttonIconName: "plus", buttonText: "Connect now!", buttonAction: {
                            showNearby = true
                            viewModel.initBle()
                        })
                    } else {
                        ForEach(viewModel.connections, id: \.self) { connection in
                            UserCard(user: connection, socials: viewModel.socials[connection.id], onViewProfile: { url in
                                guard let url else { return }
                                if let linkedin = URL(string: url) {
                                    openURL(linkedin)
                                }
                            })
                        }
                    }
                }.padding()
            }
            .refreshable {
                viewModel.refreshConnections()
            }
            .onAppear(){
                viewModel.refreshConnections()
            }.overlay(alignment: .bottomTrailing, content: {
                Button(action: {
                    showNearby = true
                    viewModel.initBle()
                }) {
                    Image(systemName: "plus").padding(5)
                }.buttonStyle(.borderedProminent).padding(20)
            }).sheet(isPresented: $showNearby, onDismiss: { viewModel.stopBle() }, content: {
                ConnectView(message: viewModel.message, users: viewModel.nearbyUsers, connectWithUser: { id in
                    viewModel.connectWithUser(requestedID: id)
                }).presentationDetents([.medium, .large])
            })
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
    var nearbyUsers: [User] = [
        User(id: "0", name: "Steve Jobs", job: "CEO"),
        User(id: "1", name: "Bill Gates", job: "Founder")
    ]
    var message: String? = "Test"
    
    func refreshConnections() {}
    func acceptConnection(uid: String) {}
    func declineConnection(uid: String) {}
    func connectWithUser(requestedID: String) {}
    func initBle() {}
    func stopBle() {}
}
