//
//  ConnectView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import SwiftUI

//Screen to connect with other users

struct ConnectView : View {
    @State var message: String?
    @State var users: [User]
    
    var connectWithUser: (String) -> Void
    
    var body : some View {
        ZStack(alignment: .bottom) {
            //Show a message to the user about the connection status (ex. connection request sent)
            if let connectionMessage = message {
                Text(connectionMessage).font(.body(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            ScrollView {
                if(users.isEmpty) {
                    Placeholder(title: "You are alone",
                                bodyText: "No nearby users",
                                iconName: "person.slash")
                } else {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())]) {
                        ForEach(users, id: \.self) { user in
                            PublicUserCard(user: user, onConnect: {uid in
                                connectWithUser(uid)
                            })
                        }
                    }.frame(maxWidth: .infinity)
                }
            }
        }
    }
}

#Preview {
    ConnectView(message: nil, users: [User(id: "0", name: "Hello", job: "Word")], connectWithUser: {_ in})
}
