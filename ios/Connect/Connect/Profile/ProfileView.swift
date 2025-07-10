//
//  ProfileView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct ProfileView : View {
    @Environment(\.openURL) private var openURL
    @StateObject private var viewModel = ProfileViewModel()
    
    var body : some View {
        VStack {
            if(viewModel.userProfile != nil) {
                UserCard(user: viewModel.userProfile!, socials: viewModel.userSocials, onViewProfile: { url in
                    if(url != nil) {
                        if let url = URL(string: url!) {
                            openURL(url)
                        }
                    }
                    
                })
            } else {
                Text("Loading...")
            }
        }.toolbar {
            NavigationLink (destination: EditProfileView()) {
                Image(systemName: "square.and.pencil")
            }
        }
        .onAppear() {
            viewModel.fetchProfile()
        }
    }
}
