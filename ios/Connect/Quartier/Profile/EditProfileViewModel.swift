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
    private var userRepository: any UserRepository = UserDatabase.shared
    private var socialsRepository: any SocialsRepository = SocialsDatabase.shared
    private var imageRepository: any ImageRepository = ImageManager()
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    @Published var saveStatus: SaveStatus? = nil
    
    init() {
        Task {
            userProfile = try await userRepository.getUser()
        }
        Task {
            userSocials = try await socialsRepository.getUserSocials()
        }
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
                    if(savedLinkedIn != nil) {
                        group.addTask {
                            try await self.socialsRepository.upsertSocials(linkedInUrl: savedLinkedIn!)
                        }
                    }
                    if(pfp != nil) {
                        group.addTask {
                            let scaledImage = await self.imageRepository.resizeTo400(image: pfp!)
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
