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
    private var userRepository = UserRepository()
    private var imageManager = ImageManager()
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    @Published var saveStatus: SaveStatus? = nil
    
    private var userID = supabase.auth.currentUser!.id.uuidString
    
    init() {
        Task {
            userProfile = try await userRepository.getUser(id: userID)
        }
        Task {
            userSocials = try await userRepository.getUserSocials(id: userID)
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
                        try await self.userRepository.updateUser(userID: self.userID, name: savedName, jobTitle: savedJobTitle)
                    }
                    if(savedLinkedIn != nil) {
                        group.addTask {
                            try await self.userRepository.upsertSocials(userID: self.userID, linkedInUrl: savedLinkedIn!)
                        }
                    }
                    if(pfp != nil) {
                        group.addTask {
                            let imageData = await self.imageManager.prepareImageForUpload(image: pfp!)
                            try await self.userRepository.uploadPfp(userID: self.userID, fileName: UUID().uuidString+".webp", imageData: imageData)
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
}

enum SaveStatus {
    case success
    case error
    case saving
}
