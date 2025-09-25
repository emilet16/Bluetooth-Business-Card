//
//  EditProfileView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-07.
//

import SwiftUI
import PhotosUI

//A screen allowing the user to change their profile

struct EditProfileView : View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    
    @State var userProfile: User?
    @State var userMessage: String?
    var matchesLinkedinRegex: (String)->Bool
    var saveUser: (String, String, String, UIImage?) -> Void
    
    
    @State var name : String = ""
    @State var jobTitle : String = ""
    
    @State var linkedInURL : String = ""
    @State var linkedinValid = true
    
    @State var newPfp: UIImage?
    
    var body : some View {
        ZStack(alignment: .bottom) {
            if let message = userMessage {
                Text(message).font(.title(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            VStack() {
                ProfileImagePicker(url: userProfile?.pfp_url, onChange: { pfp in
                    newPfp = pfp
                })
                TextField("Name", text: $name, prompt: Text("Name")).font(.body(17))
                TextField("Job Title", text: $jobTitle, prompt: Text("Job Title")).font(.body(17))
                
                TextField("LinkedIn URL", text: $linkedInURL, prompt: Text("Linkedin URL")).font(.body(17))
                    .lineLimit(3...5)
                    .onChange(of: linkedInURL) {
                        linkedinValid = linkedInURL.isEmpty || matchesLinkedinRegex(linkedInURL)
                    }
                
                if(!linkedinValid) {
                    Text("Invalid Input").foregroundStyle(.red).font(.caption)
                }
                
                HStack {
                    Button(action: {
                        dismiss()
                    }) {
                        Spacer()
                        Text("Don't save").font(.body(17))
                        Spacer()
                    }.buttonStyle(.bordered)
                    
                    Button(action: {
                        saveUser(name, jobTitle, linkedInURL, newPfp)
                    }) {
                        Spacer()
                        Text("Save").foregroundStyle(Color("OnAccentColor")).font(.body(17))
                        Spacer()
                    }.disabled(!linkedinValid).buttonStyle(.borderedProminent)
                }
            }
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .padding()
            .frame(maxHeight: .infinity)
        }
    }
}

#Preview {
    EditProfileView(userProfile: nil, matchesLinkedinRegex: {_ in true}) { _, _, _, _ in
        
    }
}
