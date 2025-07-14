//
//  EditProfileView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-07.
//

import SwiftUI
import PhotosUI

struct EditProfileView : View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @StateObject var viewModel = EditProfileViewModel()
    
    @State var name : String = ""
    @State var jobTitle : String = ""
    
    @State var linkedInURL : String = ""
    @State var linkedinValid = true
    
    @State var newPfp: UIImage?
    
    @State var userMessage: String?
    
    var body : some View {
        VStack() {
            ProfileImagePicker(url: viewModel.userProfile?.pfp_url, onChange: { pfp in
                newPfp = pfp
            })
            TextField("Name", text: $name, prompt: Text("Enter your name")).font(.roboto(17))
            TextField("Job Title", text: $jobTitle, prompt: Text("Enter your job title")).font(.roboto(17))
            
            TextField("LinkedIn URL", text: $linkedInURL, prompt: Text("Paste your linkedin URL here")).font(.roboto(17))
                .onChange(of: linkedInURL) {
                    linkedinValid = linkedInURL.isEmpty || matchesLinkedinRegex(input: linkedInURL)
                }
            
            if(!linkedinValid) {
                Text("Invalid Input").foregroundStyle(.red).font(.caption)
            }
            
            Button(action: {
                viewModel.saveUser(name: name, jobTitle: jobTitle, linkedInURL: linkedInURL, pfp: newPfp)
            }) {
                Text("Submit").foregroundStyle(Color("OnAccentColor")).font(.roboto(17))
            }.disabled(!linkedinValid).buttonStyle(.borderedProminent)
            
            
            if(userMessage != nil) {
                Text(userMessage!)
            }
        }
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Edit Profile").font(.poppins(24))
            }
        }
        .textFieldStyle(RoundedBorderTextFieldStyle())
        .padding()
        .onChange(of: viewModel.saveStatus) {
            switch(viewModel.saveStatus) {
            case .success:
                dismiss()
            case .saving:
                userMessage = "Saving..."
            case .error:
                userMessage = "Error saving, please try again"
            default:
                userMessage = nil
            }
        }
    }
}

func matchesLinkedinRegex(input: String) -> Bool {
    let regex = try! Regex("^https://www\\.linkedin\\.com/in/[^/]+/?$")
    return try! regex.wholeMatch(in: input) != nil
}
