//
//  ProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-06.
//

import Foundation

@MainActor
class ProfileViewModel: ObservableObject {
    private var userDatabase = UserDatabase.shared
    private var socialsDatabase = SocialsDatabase.shared
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    
    func fetchProfile() {
        Task {
            userProfile = try await userDatabase.getUser()
        }
        Task {
            userSocials = try await socialsDatabase.getUserSocials()
        }
    }
}
