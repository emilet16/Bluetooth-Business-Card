//
//  ProfileView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

//Screen for the user to see their profile

struct ProfileView<T: ProfileViewModel> : View {
    @Environment(\.openURL) private var openURL
    @StateObject var viewModel: T
    
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
                NavigationLink (destination: EditProfileView(viewModel: EditProfileViewModelImpl())) {
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
    ProfileView(viewModel: MockProfileVM())
}

class MockProfileVM : ProfileViewModel {
    var userProfile: User? = User(id: "0", name: "Steve Jobs", job: "CEO")
    var userSocials: Socials? = nil
    
    func fetchProfile() {}
}
