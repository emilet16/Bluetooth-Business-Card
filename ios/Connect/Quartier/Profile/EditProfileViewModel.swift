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
    private var userDatabase = UserDatabase.shared
    private var socialsDatabase = SocialsDatabase.shared
    private var imageManager = ImageManager()
    
    @Published var userProfile: User? = nil
    @Published var userSocials: Socials? = nil
    @Published var saveStatus: SaveStatus? = nil
    
    init() {
        Task {
            userProfile = try await userDatabase.getUser()
        }
        Task {
            userSocials = try await socialsDatabase.getUserSocials()
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
                        try await self.userDatabase.updateUser(name: savedName, jobTitle: savedJobTitle)
                    }
                    if(savedLinkedIn != nil) {
                        group.addTask {
                            try await self.socialsDatabase.upsertSocials(linkedInUrl: savedLinkedIn!)
                        }
                    }
                    if(pfp != nil) {
                        group.addTask {
                            let imageData = await self.imageManager.prepareImageForUpload(image: pfp!)
                            try await self.userDatabase.uploadPfp(fileName: UUID().uuidString+".webp", imageData: imageData)
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
