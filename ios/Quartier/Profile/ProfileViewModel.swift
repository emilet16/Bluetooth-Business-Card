//
//  ProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-06.
//

import Foundation
import PhotosUI

//Viewmodel for the profile screen, fecthes the user's profile
@MainActor
protocol ProfileViewModel : ObservableObject {
    var userProfile: User? { get }
    var userSocials: Socials? { get }
    var userMessage: String? { get }
    var dismissEdit: Bool { get }
    
    func fetchProfile()
    func saveUser(name: String, jobTitle: String, linkedInURL: String, pfp: UIImage?)
    func matchesLinkedinRegex(input: String) -> Bool
}

class ProfileViewModelImpl: ProfileViewModel {
    private var userRepository: any UserRepository
    private var socialsRepository: any SocialsRepository
    private var imageRepository: any ImageRepository
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    @Published var userMessage: String? = nil
    @Published var dismissEdit: Bool = false
    
    init(userRepository: any UserRepository = UserDatabase.shared, socialsRepository: any SocialsRepository = SocialsDatabase.shared, imageRepository: any ImageRepository = ImageManager()) {
        self.userRepository = userRepository
        self.socialsRepository = socialsRepository
        self.imageRepository = imageRepository
    }
    
    func fetchProfile() {
        Task {
            userProfile = try await userRepository.getUser()
        }
        Task {
            userSocials = try await socialsRepository.getUserSocials()
        }
    }
    
    func saveUser(name: String, jobTitle: String, linkedInURL: String, pfp: UIImage?) {
        guard let userProfile else { return }
        
        userMessage = "Saving..."
        
        //If the user didn't change it, keep it the way it was
        let savedName = name.isEmpty ? userProfile.name : name
        let savedJobTitle = jobTitle.isEmpty ? userProfile.job: jobTitle
        let savedLinkedIn = linkedInURL.isEmpty ? userSocials?.linkedin_url : linkedInURL
        
        Task {
            do {
                try await withThrowingTaskGroup(of: Void.self) { group in
                    //Save everything simultaneously
                    group.addTask {
                        try await self.userRepository.updateUser(name: savedName, jobTitle: savedJobTitle)
                    }
                    if let linkedin = savedLinkedIn {
                        group.addTask {
                            try await self.socialsRepository.upsertSocials(linkedInUrl: linkedin)
                        }
                    }
                    if let pfpImage = pfp {
                        group.addTask {
                            let scaledImage = await self.imageRepository.resizeTo400(image: pfpImage) //Crop image
                            let imageData = try await self.imageRepository.encodeToWebP(image: scaledImage) //Convert to WebP
                            try await self.userRepository.uploadPfp(fileName: UUID().uuidString+".webp", imageData: imageData) //Save image
                        }
                    }
                    
                    for try await _ in group {}
                    
                    return (Void())
                }
            }
            catch(let error) {
                print(error)
                userMessage = "An error happened saving your profile!"
                return
            }
            userMessage = "Profile saved successfully!"
            dismissEdit = true
            Task {
                try await Task.sleep(for: .seconds(5))
                userMessage = nil
                dismissEdit = false
            }
        }
    }
    
    nonisolated func matchesLinkedinRegex(input: String) -> Bool { //Check if the link is a valid linkedin link
        do {
            let regex = try Regex("^https://www\\.linkedin\\.com/in/[^/]+/?$")
            return try regex.wholeMatch(in: input) != nil
        } catch {
            return false
        }
    }
}
