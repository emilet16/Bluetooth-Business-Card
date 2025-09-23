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
            Spacer()
            HStack {
                ProfileImage(url: user.pfp_url, size: 80)
                Spacer()
                VStack(alignment: .trailing) {
                    Text(user.name).font(.bodyBold(17))
                    Text(user.job).font(.body(17))
                }
            }
            Spacer()
            HStack(spacing: 20) {
                Button(action: {onDecline(user.id)}) {
                    Image(systemName: "xmark")
                        .frame(width: 75, height: 30)
                }.buttonStyle(.bordered)
                
                Button(action: {onAccept(user.id)}) {
                    Image(systemName: "checkmark")
                        .frame(width: 75, height: 30)
                }.buttonStyle(.borderedProminent)
            }
            Spacer()
        }
        .frame(width: 225, height: 180)
        .padding(20)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

#Preview {
    ConnectionRequest(user: User(id: "0", name: "Steve Jobs", job: "CEO"), onAccept: {_ in}, onDecline: {_ in})
        .frame(height: 180)
}
