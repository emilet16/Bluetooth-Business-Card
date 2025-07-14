//
//  ConnectedView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

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
