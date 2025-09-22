//
//  LoginViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation
import Auth

//Viewmodel for the login screen, handles the sign in process
@MainActor
protocol LoginViewModel : ObservableObject {
    var message: String? { get }
    
    func login(email: String, pwd: String)
    func matchEmailRegex(text: String) -> Bool
}

class LoginViewModelImpl : LoginViewModel {
    private var authManager: any AuthManager
    
    @Published var message: String? = nil
    
    init(authManager: any AuthManager = AuthManagerImpl.shared) {
        self.authManager = authManager
    }
    
    func login(email: String, pwd: String) {
        Task {
            do {
                try await authManager.login(email: email, pwd: pwd)
            } catch(let error as AuthError) {
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
