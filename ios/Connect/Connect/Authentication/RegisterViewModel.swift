//
//  RegisterViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation

@MainActor
class RegisterViewModel : ObservableObject {
    private var authManager: AuthManager = AuthManager.shared
    
    func signup(email: String, pwd: String, name: String) {
        Task {
            try await authManager.signup(email: email, pwd: pwd, name: name)
        }
    }
}
