//
//  ConnectVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
@testable import Quartier

@MainActor
struct ProfileVMTests {
    @Test func refreshUser() async throws {
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let viewModel = ProfileViewModel(userRepository: userRepo, socialsRepository: socialsRepo)
        
        let _ = viewModel.$userProfile.dropFirst().sink { profile in
            #expect(profile == User(id: "0", name: "name", job: "job"))
        }
        
        let _ = viewModel.$userSocials.dropFirst().sink { socials in
            #expect(socials == Socials(id: "0", linkedin_url: "link"))
        }
        
        viewModel.fetchProfile()
    }
}
