//
//  LoginViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation
import Auth

@MainActor
class LoginViewModel : ObservableObject {
    private var authManager: AuthManager = AuthManager.shared
    
    @Published var message: String?
    
    func login(email: String, pwd: String) {
        Task {
            do {
                try await authManager.login(email: email, pwd: pwd)
            } catch(let error as AuthError) {
                message = error.localizedDescription
            }
        }
    }
    
    func matchEmailRegex(text: String) -> Bool {
        let emailRegex = try! Regex("^[^@]+@[^@]+\\.[^@]+$")
        return try! emailRegex.wholeMatch(in: text) != nil
    }
}
