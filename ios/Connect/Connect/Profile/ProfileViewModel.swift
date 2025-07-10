//
//  ProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-06.
//

import Foundation

@MainActor
class ProfileViewModel: ObservableObject {
    private var userRepository = UserRepository()
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    
    private var userID = supabase.auth.currentUser!.id.uuidString
    
    func fetchProfile() {
        Task {
            userProfile = try await userRepository.getUser(id: userID)
        }
        Task {
            userSocials = try await userRepository.getUserSocials(id: userID)
        }
    }
}
