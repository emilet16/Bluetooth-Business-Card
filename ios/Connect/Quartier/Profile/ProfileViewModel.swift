//
//  ProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-06.
//

import Foundation

protocol ProfileViewModel : ObservableObject {
    var userProfile: User? { get }
    var userSocials: Socials? { get }
    
    func fetchProfile()
}

@MainActor
class ProfileViewModelImpl: ProfileViewModel {
    private var userRepository: any UserRepository
    private var socialsRepository: any SocialsRepository
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    
    init(userRepository: any UserRepository = UserDatabase.shared, socialsRepository: any SocialsRepository = SocialsDatabase.shared) {
        self.userRepository = userRepository
        self.socialsRepository = socialsRepository
    }
    
    func fetchProfile() {
        Task {
            userProfile = try await userRepository.getUser()
        }
        Task {
            userSocials = try await socialsRepository.getUserSocials()
        }
    }
}
