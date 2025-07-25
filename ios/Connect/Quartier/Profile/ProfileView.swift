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
        ScrollView {
            VStack {
                if let userProfile = viewModel.userProfile {
                    UserCard(user: userProfile, socials: viewModel.userSocials, onViewProfile: { url in
                        if let urlString = url {
                            if let link = URL(string: urlString) {
                                openURL(link)
                            }
                        }
                    })
                }
            }
        }.toolbar {
            ToolbarItem(placement: .principal) {
                Text("Profile").font(.poppins(24))
            }
            ToolbarItem(placement: .primaryAction) {
                NavigationLink (destination: EditProfileView()) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
        .onAppear() {
            viewModel.fetchProfile()
        }
        .refreshable {
            viewModel.fetchProfile()
        }
    }
}

#Preview {
    ProfileView()
}
