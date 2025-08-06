//
//  RequestView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

//Shows a connection request from another user

struct ConnectionRequest : View {
    let user: User
    var onAccept: (String) -> Void = { _ in }
    var onDecline: (String) -> Void = { _ in }
    
    var body: some View {
        VStack {
            ProfileImage(url: user.pfp_url)
            Text(user.name).font(.roboto(17))
            Text(user.job).font(.roboto(17))
            Spacer()
            HStack(spacing: 20) {
                Button(action: {onAccept(user.id)}) {
                    Image(systemName: "checkmark")
                }
                Button(action: {onDecline(user.id)}) {
                    Image(systemName: "xmark")
                }
            }
        }
        .frame(width: 150)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

#Preview {
    ConnectionRequest(user: User(id: "0", name: "Steve Jobs", job: "CEO"), onAccept: {_ in}, onDecline: {_ in})
        .frame(height: 180)
}
