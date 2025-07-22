//
//  RegisterVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-21.
//

import Testing
@testable import Quartier

@MainActor
struct RegisterVMTests {
    @Test func emailSignup_valid() async throws {
        let viewModel = RegisterViewModel(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == nil)
        }
        
        viewModel.signup(email: "newemail@gmail.com", pwd: "Password", name: "Name")
    }
    
    @Test func emailLogin_invalid() async throws {
        let viewModel = RegisterViewModel(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(viewModel.message == "User already exists")
        }
        
        viewModel.signup(email: "email@gmail.com", pwd: "Password", name: "Name")
    }
    
    @Test func emailRegex_valid() async throws {
        let viewModel = RegisterViewModel(authManager: MockAuthManager())
        #expect(viewModel.matchEmailRegex(text: "email@gmail.com"))
    }
    
    @Test func emailRegex_invalidEnd() async throws {
        let viewModel = RegisterViewModel(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "email@gmail"))
    }
    
    @Test func emailRegex_invalidStart() async throws {
        let viewModel = RegisterViewModel(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "em@il@gmail.com"))
    }
    
    @Test func emailRegex_invalidEmail() async throws {
        let viewModel = RegisterViewModel(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "emailgmail.com"))
    }
}
