//
//  ConnectedView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

//User card shown to connected users

struct UserCard : View {
    let user: User
    let socials: Socials?
    var onViewProfile: (String?) -> Void = { _ in }
    
    var body: some View {
        Button(action: {
            onViewProfile(socials?.linkedin_url)
        }) {
            HStack {
                ProfileImage(url: user.pfp_url, size: 150)
                Spacer()
                VStack {
                    Text(user.name).font(.roboto(17))
                    Text(user.job).font(.roboto(17))
                }
                Spacer()
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
        .buttonStyle(.borderless)
        .frame(maxWidth: .infinity)
        .padding()
    }
}

#Preview {
    UserCard(user: User(id: "0", name: "Steve Jobs", job: "CEO"), socials: nil, onViewProfile: {_ in})
}
