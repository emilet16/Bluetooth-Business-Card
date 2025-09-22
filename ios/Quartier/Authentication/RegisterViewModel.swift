//
//  RegisterViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation
import Auth

//Viewmodel for the registration screen, handles the sign up process
@MainActor
protocol RegisterViewModel : ObservableObject {
    var message: String? { get }
    func signup(email: String, pwd: String, name: String)
    func matchEmailRegex(text: String) -> Bool
}

class RegisterViewModelImpl : RegisterViewModel {
    private var authManager: any AuthManager
    
    @Published var message: String? = nil
    
    init(authManager: any AuthManager = AuthManagerImpl.shared) {
        self.authManager = authManager
    }
    
    func signup(email: String, pwd: String, name: String) {
        Task {
            do {
                try await authManager.signup(email: email, pwd: pwd, name: name)
            }  catch(let error as AuthError) {
                message = error.message
            }
        }
    }
    
    nonisolated func matchEmailRegex(text: String) -> Bool {
        do {
            let emailRegex = try Regex("^[^@]+@[^@]+\\.[^@]+$")
            return try emailRegex.wholeMatch(in: text) != nil
        } catch {
            return false
        }
    }
}
