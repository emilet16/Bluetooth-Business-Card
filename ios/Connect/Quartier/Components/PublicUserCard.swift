//
//  ProfileView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

struct PublicUserCard : View {
    let user: User
    var onConnect: (String) -> Void = { _ in }
    
    var body: some View {
        Button(action: {
            onConnect(user.id)
        }) {
            VStack {
                ProfileImage(url: user.pfp_url)
                Text(user.name).font(.roboto(17))
                Text(user.job).font(.roboto(17))
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
        .buttonStyle(.borderless)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}
	
