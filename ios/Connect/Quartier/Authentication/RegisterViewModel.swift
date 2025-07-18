//
//  RegisterViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation
import Auth

@MainActor
class RegisterViewModel : ObservableObject {
    private var authManager: any AuthManager = AuthManagerImpl.shared
    
    @Published var message: String?
    
    func signup(email: String, pwd: String, name: String) {
        Task {
            do {
                try await authManager.signup(email: email, pwd: pwd, name: name)
            }  catch(let error as AuthError) {
                message = error.message
            }
        }
    }
    
    func matchEmailRegex(text: String) -> Bool {
        let emailRegex = try! Regex("^[^@]+@[^@]+\\.[^@]+$")
        return try! emailRegex.wholeMatch(in: text) != nil
    }
}
