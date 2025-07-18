//
//  ProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-06.
//

import Foundation

@MainActor
class ProfileViewModel: ObservableObject {
    private var userRepository: any UserRepository = UserDatabase.shared
    private var socialsRepository: any SocialsRepository = SocialsDatabase.shared
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    
    func fetchProfile() {
        Task {
            userProfile = try await userRepository.getUser()
        }
        Task {
            userSocials = try await socialsRepository.getUserSocials()
        }
    }
}
