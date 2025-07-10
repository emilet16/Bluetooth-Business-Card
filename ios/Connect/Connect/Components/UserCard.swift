//
//  ConnectedView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

struct ConnectedUser : View {
    let user: User
    let socials: Socials?
    var onViewProfile: (String?) -> Void = { _ in }
    
    var body: some View {
        Button(action: {
            onViewProfile(socials?.linkedin_url)
        }) {
            VStack {
                ProfileImageView(url: user.pfp_url)
                Text(user.name)
                Text(user.job)
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
        .buttonStyle(.borderless)
    }
}
