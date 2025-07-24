//
//  EditProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-07.
//

import Foundation
import PhotosUI

@MainActor
class EditProfileViewModel: ObservableObject {
    private var userRepository: any UserRepository
    private var socialsRepository: any SocialsRepository
    private var imageRepository: any ImageRepository
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    @Published var saveStatus: SaveStatus? = nil
    
    init(userRepository: any UserRepository = UserDatabase.shared, socialsRepository: any SocialsRepository = SocialsDatabase.shared, imageRepository: any ImageRepository = ImageManager()) {
        self.userRepository = userRepository
        self.socialsRepository = socialsRepository
        self.imageRepository = imageRepository
    }
    
    func refreshUser() {
        Task {
            try await getUser()
        }
        Task {
            try await getUserSocials()
        }
    }
    
    func getUser() async throws {
        userProfile = try await userRepository.getUser()
    }
    
    func getUserSocials() async throws {
        userSocials = try await socialsRepository.getUserSocials()
    }
    
    func saveUser(name: String, jobTitle: String, linkedInURL: String, pfp: UIImage?) {
        saveStatus = SaveStatus.saving
        
        let savedName = name.isEmpty ? userProfile!.name : name
        let savedJobTitle = jobTitle.isEmpty ? userProfile!.job: jobTitle
        let savedLinkedIn = linkedInURL.isEmpty ? userSocials?.linkedin_url : linkedInURL
        
        Task {
            do {
                try await withThrowingTaskGroup(of: Void.self) { group in
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
                            let scaledImage = await self.imageRepository.resizeTo400(image: pfpImage)
                            let imageData = await self.imageRepository.encodeToWebP(image: scaledImage)
                            try await self.userRepository.uploadPfp(fileName: UUID().uuidString+".webp", imageData: imageData)
                        }
                    }
                    
                    for try await _ in group {}
                    
                    return (Void())
                }
            }
            catch(let error) {
                print(error)
                saveStatus = SaveStatus.error
                return
            }
            saveStatus = SaveStatus.success
        }
    }
    
    func matchesLinkedinRegex(input: String) -> Bool {
        let regex = try! Regex("^https://www\\.linkedin\\.com/in/[^/]+/?$")
        return try! regex.wholeMatch(in: input) != nil
    }
}

enum SaveStatus {
    case success
    case error
    case saving
}
