//
//  ContentView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-17.
//

import SwiftUI

//Navigation control for the app, behavior controlled by MainViewModel

struct ContentView: View {
    @StateObject private var viewModel = MainViewModel()
    
    var body: some View {
        Group {
            switch(viewModel.page) {
            case .main:
                TabView {
                    NavigationStack {
                        ConnectionsView(viewModel: ConnectionsViewModelImpl())
                    }.tabItem() {
                        Image(systemName: "person.3")
                        Text("Connections")
                    }
                    
                    NavigationStack {
                        ProfileView(viewModel: ProfileViewModelImpl())
                    }.tabItem() {
                        Image(systemName: "person.circle")
                        Text("Profile")
                    }
                }
            case .login:
                NavigationStack {
                    LoginView(viewModel: LoginViewModelImpl())
                }
            case .loading:
                ProgressView()
            }
        }
    }
}
