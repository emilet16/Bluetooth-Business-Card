//
//  AuthManager.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import Foundation

@MainActor
class AuthManager : ObservableObject {
    @Published var userID: String? = nil
    
    private var authListener: Task<Void, Never>?
    
    init() {
        authListener = Task {
            for await (_, session) in supabase.auth.authStateChanges {
                userID = session?.user.id.uuidString
            }
        }
    }
    
    deinit {
        authListener?.cancel()
    }
    
    func login(email:String, pwd: String) {
        Task {
            try await supabase.auth.signIn(email: email, password: pwd)
        }
    }
    
    func signup(email:String, pwd: String, name: String) {
        Task {
            try await supabase.auth.signUp(email: email, password: pwd, data: [
                "name": .string(name),
            ])
        }
    }
    
    func logout() {
        Task {
            try await supabase.auth.signOut()
        }
    }
}
