//
//  AuthManager.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//
import Foundation

protocol AuthManager {
    func login(email:String, pwd: String) async throws
    func signup(email:String, pwd: String, name: String) async throws
    func signout() async throws
}

class AuthManagerImpl : AuthManager {
    static let shared = AuthManagerImpl()
    
    func login(email:String, pwd: String) async throws {
        try await supabase.auth.signIn(email: email, password: pwd)
    }
    
    func signup(email:String, pwd: String, name: String) async throws {
        try await supabase.auth.signUp(email: email, password: pwd, data: [
            "name": .string(name),
        ])
    }
    
    func signout() async throws {
        try await supabase.auth.signOut()
    }
}
