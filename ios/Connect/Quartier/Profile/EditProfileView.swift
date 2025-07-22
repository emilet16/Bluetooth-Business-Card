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
        ZStack(alignment: .bottom) {
            if let message = userMessage {
                Text(message).font(.roboto(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            VStack() {
                ProfileImagePicker(url: viewModel.userProfile?.pfp_url, onChange: { pfp in
                    newPfp = pfp
                })
                TextField("Name", text: $name, prompt: Text("Enter your name")).font(.roboto(17))
                TextField("Job Title", text: $jobTitle, prompt: Text("Enter your job title")).font(.roboto(17))
                
                TextField("LinkedIn URL", text: $linkedInURL, prompt: Text("Paste your linkedin URL here")).font(.roboto(17))
                    .onChange(of: linkedInURL) {
                        linkedinValid = linkedInURL.isEmpty || viewModel.matchesLinkedinRegex(input: linkedInURL)
                    }
                
                if(!linkedinValid) {
                    Text("Invalid Input").foregroundStyle(.red).font(.caption)
                }
                
                Button(action: {
                    viewModel.saveUser(name: name, jobTitle: jobTitle, linkedInURL: linkedInURL, pfp: newPfp)
                }) {
                    Text("Submit").foregroundStyle(Color("OnAccentColor")).font(.roboto(17))
                }.disabled(!linkedinValid).buttonStyle(.borderedProminent)
            }
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .padding()
            .frame(maxHeight: .infinity)
        }
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Edit Profile").font(.poppins(24))
            }
        }
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
