//
//  ProfileView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

//Screen for the user to see their profile

struct ProfileView<T: ProfileViewModel> : View {
    @Environment(\.openURL) private var openURL
    @StateObject var viewModel: T
    @State var showEdit = false
    
    var body : some View {
        ZStack {
            if let message = viewModel.userMessage {
                Text(message).font(.title(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            ScrollView {
                VStack {
                    Spacer()
                    if let userProfile = viewModel.userProfile {
                        UserCard(user: userProfile, socials: viewModel.userSocials, onViewProfile: { url in
                            if let urlString = url {
                                if let link = URL(string: urlString) {
                                    openURL(link)
                                }
                            }
                        })
                    }
                    Spacer()
                }
            }.frame(maxHeight: .infinity)
                .onAppear() {
                    viewModel.fetchProfile()
                }
                .refreshable {
                    viewModel.fetchProfile()
                }.overlay(alignment: .bottomTrailing, content: {
                    Button(action: {
                        showEdit = true
                    }) {
                        Image(systemName: "pencil").padding(5)
                    }.buttonStyle(.borderedProminent).padding(20)
                }).sheet(isPresented: $showEdit, content: {
                    EditProfileView(userProfile: viewModel.userProfile, userMessage: viewModel.userMessage,
                                    matchesLinkedinRegex: {value in viewModel.matchesLinkedinRegex(input: value)},
                                    saveUser: {name, job, linkedin, pfp in
                        viewModel.saveUser(name: name, jobTitle: job, linkedInURL: linkedin, pfp: pfp)
                    })
                    .presentationDetents([.medium, .large])
                })
                .onChange(of: viewModel.dismissEdit, {
                    showEdit = false
                })
        }
    }
}

#Preview {
    ProfileView(viewModel: MockProfileVM())
}

class MockProfileVM : ProfileViewModel {
    var userProfile: User? = User(id: "0", name: "Steve Jobs", job: "CEO")
    var userSocials: Socials? = nil
    var userMessage: String? = nil
    var dismissEdit: Bool = false
    
    func fetchProfile() {}
    func saveUser(name: String, jobTitle: String, linkedInURL: String, pfp: UIImage?) {}
    func matchesLinkedinRegex(input: String) -> Bool {true}
}
