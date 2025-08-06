//
//  RegisterVMTests.swift
//  QuartierTests
//
//  Created by Emile Turcotte on 2025-07-21.
//

import Auth
import Foundation
import Testing
@testable import Quartier

//Tests for the login viewmodel

@MainActor
struct LoginVMTests {
    @Test func emailLogin_valid() async throws { //Normal login case
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(message == nil)
        }
        
        viewModel.login(email: "email@gmail.com", pwd: "Password")
    }
    
    @Test func emailLogin_invalid() async throws { //Wrong password login
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(viewModel.message == "Invalid login credentials")
        }
        
        viewModel.login(email: "email@gmail.com", pwd: "WrongPassword")
    }
    
    @Test func emailLogin_nonExistent() async throws { //User doesn't exist
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        
        let _ = viewModel.$message.dropFirst().sink { message in
            #expect(viewModel.message == "Invalid login credentials")
        }
        
        viewModel.login(email: "wrong_email@gmail.com", pwd: "Password")
    }
    
    @Test func emailRegex_valid() async throws { //Valid email
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        #expect(viewModel.matchEmailRegex(text: "email@gmail.com"))
    }
    
    @Test func emailRegex_invalidEnd() async throws { //Missing TLD
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "email@gmail"))
    }
    
    @Test func emailRegex_invalidStart() async throws { //Extra @
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "em@il@gmail.com"))
    }
    
    @Test func emailRegex_invalidEmail() async throws { //Missing @
        let viewModel = LoginViewModelImpl(authManager: MockAuthManager())
        #expect(!viewModel.matchEmailRegex(text: "emailgmail.com"))
    }
}
