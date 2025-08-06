//
//  EditProfileViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-07.
//

import Foundation
import PhotosUI

//The viewmodel for the Edit Profile screen, handle fetching and saving user's profile

protocol EditProfileViewModel : ObservableObject {
    var userProfile: User? { get set }
    var userSocials: Socials? { get set }
    var saveStatus: SaveStatus? { get set }
    
    func refreshUser()
    func saveUser(name: String, jobTitle: String, linkedInURL: String, pfp: UIImage?)
    func matchesLinkedinRegex(input: String) -> Bool
}

@MainActor
class EditProfileViewModelImpl : EditProfileViewModel {
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
        
        //If the user didn't change it, keep it the way it was
        let savedName = name.isEmpty ? userProfile!.name : name
        let savedJobTitle = jobTitle.isEmpty ? userProfile!.job: jobTitle
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
                            let imageData = await self.imageRepository.encodeToWebP(image: scaledImage) //Convert to WebP
                            try await self.userRepository.uploadPfp(fileName: UUID().uuidString+".webp", imageData: imageData) //Save image
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
    
    func matchesLinkedinRegex(input: String) -> Bool { //Check if the link is a valid linkedin link
        let regex = try! Regex("^https://www\\.linkedin\\.com/in/[^/]+/?$")
        return try! regex.wholeMatch(in: input) != nil
    }
}

enum SaveStatus {
    case success
    case error
    case saving
}
