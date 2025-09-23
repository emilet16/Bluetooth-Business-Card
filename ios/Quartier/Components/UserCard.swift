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
            HStack(alignment: .top) {
                ProfileImage(url: user.pfp_url, size: 100)
                Spacer()
                VStack(alignment: .trailing) {
                    Text(user.name).font(.titleBold(24))
                    Text(user.job).font(.body(17))
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
        .buttonStyle(.borderless)
        .frame(maxWidth: .infinity, idealHeight: 150)
        .padding(20)
    }
}

#Preview {
    UserCard(user: User(id: "0", name: "Steve Jobs", job: "CEO"), socials: nil, onViewProfile: {_ in})
}
