//
//  RegisterVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-21.
//

import Testing
@testable import Quartier

//Testing the register viewmodel

@MainActor
struct RegisterVMTests {
    @Test func emailSignup_valid() async throws { //Normal registration case
        let viewModel = RegisterViewModelImpl(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == nil)
        }
        
        viewModel.signup(email: "newemail@gmail.com", pwd: "Password", name: "Name")
    }
    
    @Test func emailLogin_invalid() async throws { //User already exists
        let viewModel = RegisterViewModelImpl(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(viewModel.message == "User already exists")
        }
        
        viewModel.signup(email: "email@gmail.com", pwd: "Password", name: "Name")
    }
    
    @Test func emailRegex_valid() async throws { //Valid email
        let viewModel = RegisterViewModelImpl(authManager: MockAuthManager())
        #expect(viewModel.matchEmailRegex(text: "email@gmail.com"))
    }
    
    @Test func emailRegex_invalidEnd() async throws { //Missing TLD
        let viewModel = RegisterViewModelImpl(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "email@gmail"))
    }
    
    @Test func emailRegex_invalidStart() async throws { //Extra @
        let viewModel = RegisterViewModelImpl(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "em@il@gmail.com"))
    }
    
    @Test func emailRegex_invalidEmail() async throws { //Missing @
        let viewModel = RegisterViewModelImpl(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "emailgmail.com"))
    }
}
