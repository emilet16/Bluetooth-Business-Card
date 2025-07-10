//
//  RequestView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

struct ConnectionRequest : View {
    let user: User
    var onAccept: (String) -> Void = { _ in }
    var onDecline: (String) -> Void = { _ in }
    
    var body: some View {
        VStack {
            ProfileImage(url: user.pfp_url)
            Text(user.name)
            Text(user.job)
            HStack {
                Button(action: {onAccept(user.id)}) {
                    Image(systemName: "checkmark")
                }
                Button(action: {onDecline(user.id)}) {
                    Image(systemName: "xmark")
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}
