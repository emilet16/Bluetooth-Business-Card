//
//  LoginViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation

@MainActor
class LoginViewModel : ObservableObject {
    private var authManager: AuthManager = AuthManager.shared
    
    func login(email: String, pwd: String) {
        Task {
            try await authManager.login(email: email, pwd: pwd)
        }
    }
}
