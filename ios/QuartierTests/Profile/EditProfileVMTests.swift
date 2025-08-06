//
//  ConnectVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-23.
//

import PhotosUI
import Foundation
import Testing
@testable import Quartier

//Test the edit profile view model

@MainActor
struct EditProfileVMTests {
    @Test func refreshUser() async throws { //Fetch the user's profile
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo)
        
        let _ = viewModel.$userProfile.dropFirst().sink { profile in
            #expect(profile == User(id: "0", name: "name", job: "job"))
        }
        
        let _ = viewModel.$userSocials.dropFirst().sink { socials in
            #expect(socials == Socials(id: "0", linkedin_url: "link"))
        }
        
        viewModel.refreshUser()
    }
    
    @Test func saveUser_success() async throws { //Everything saves as expected
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .success)
            Task {
                let user = try await userRepo.getUser()
                #expect(user.name == "newname")
                #expect(user.job.rawValue == "newjob")
                #expect(user.pfp_url == "https://google.com")
            }
            Task {
                let socials = try await socialsRepo.getUserSocials()
                #expect(socials.linkedin_url == "linkedin")
            }
        }
        
        if let data = Data(base64Encoded: "https://google.com", options: .ignoreUnknownCharacters) { //Encode the path as a UIImage, so it can be decoded by the mock classes later
            viewModel.saveUser(name: "newname", jobTitle: "newjob", linkedInURL: "linkedin", pfp: UIImage(data: data))
        }
    }
    
    @Test func saveUser_socialsError() async throws { //Error while saving socials
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        
        socialsRepo.error = true
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .error)
        }
        
        if let data = Data(base64Encoded: "https://google.com", options: .ignoreUnknownCharacters) {
            viewModel.saveUser(name: "newname", jobTitle: "newjob", linkedInURL: "linkedin", pfp: UIImage(data: data))
        }
    }
    
    @Test func saveUser_profileError() async throws { //Error while saving profile
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        
        userRepo.profileError = true
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .error)
        }
        
        if let data = Data(base64Encoded: "https://google.com", options: .ignoreUnknownCharacters) {
            viewModel.saveUser(name: "newname", jobTitle: "newjob", linkedInURL: "linkedin", pfp: UIImage(data: data))
        }
    }
    
    @Test func saveUser_pfpError() async throws { //Error while saving pfp
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        
        userRepo.pfpError = true
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .error)
        }
        
        if let data = Data(base64Encoded: "https://google.com", options: .ignoreUnknownCharacters) {
            viewModel.saveUser(name: "newname", jobTitle: "newjob", linkedInURL: "linkedin", pfp: UIImage(data: data))
        }
    }
    
    @Test func saveUser_noChangeSocials() async throws { //Keep socials the same!
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .success)
            Task {
                let user = try await userRepo.getUser()
                #expect(user.name == "newname")
                #expect(user.job.rawValue == "newjob")
                #expect(user.pfp_url == "https://google.com")
            }
            Task {
                let socials = try await socialsRepo.getUserSocials()
                #expect(socials.linkedin_url == "link")
            }
        }
        
        if let data = Data(base64Encoded: "https://google.com", options: .ignoreUnknownCharacters) {
            viewModel.saveUser(name: "newname", jobTitle: "newjob", linkedInURL: "", pfp: UIImage(data: data))
        }
    }
    
    @Test func saveUser_noChangePfp() async throws { //Keep pfp the same!
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .success)
            Task {
                let user = try await userRepo.getUser()
                #expect(user.name == "newname")
                #expect(user.job.rawValue == "newjob")
                #expect(user.pfp_url == nil)
            }
            Task {
                let socials = try await socialsRepo.getUserSocials()
                #expect(socials.linkedin_url == "linkedin")
            }
        }
        
        if let data = Data(base64Encoded: "", options: .ignoreUnknownCharacters) {
            viewModel.saveUser(name: "newname", jobTitle: "newjob", linkedInURL: "linkedin", pfp: UIImage(data: data))
        }
    }
    
    @Test func saveUser_noChangeProfile() async throws { //Keep profile the same!
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        try await viewModel.getUser()
        try await viewModel.getUserSocials()
        
        let _ = viewModel.$saveStatus.dropFirst().sink { status in
            #expect(status == .saving)
        }
        
        let _ = viewModel.$saveStatus.dropFirst().dropFirst().sink { status in
            #expect(status == .success)
            Task {
                let user = try await userRepo.getUser()
                #expect(user.name == "name")
                #expect(user.job.rawValue == "job")
                #expect(user.pfp_url == "https://google.com")
            }
            Task {
                let socials = try await socialsRepo.getUserSocials()
                #expect(socials.linkedin_url == "linkedin")
            }
        }
        
        if let data = Data(base64Encoded: "https://google.com", options: .ignoreUnknownCharacters) {
            viewModel.saveUser(name: "", jobTitle: "", linkedInURL: "linkedin", pfp: UIImage(data: data))
        }
    }
    
    @Test func linkedinRegex_valid() async throws { //Valid linkedin profile link
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        #expect(viewModel.matchesLinkedinRegex(input: "https://www.linkedin.com/in/user"))
    }
    
    @Test func linkedinRegex_google() async throws { //Not a linkedin link
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        #expect(!viewModel.matchesLinkedinRegex(input: "https://www.google.com/search?query=query"))
    }
    
    @Test func linkedinRegex_invalidLink() async throws { //Missing https, could be changed in the future
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        #expect(!viewModel.matchesLinkedinRegex(input: "www.linkedin.com/in/user"))
    }
    
    @Test func linkedinRegex_noUser() async throws { //Missing user in the profile link
        let userRepo = MockUserRepo()
        let socialsRepo = MockSocialsRepo()
        let imageRepo = MockImageRepo()
        let viewModel = EditProfileViewModelImpl(userRepository: userRepo, socialsRepository: socialsRepo, imageRepository: imageRepo)
        #expect(!viewModel.matchesLinkedinRegex(input: "https://www.linkedin.com/in/"))
    }
}
