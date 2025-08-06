//
//  MockAuthManager.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-21.
//

import Auth
import Foundation
@testable import Quartier

//Mock the behavior of the auth manager for viewmodel tests, checked against the accounts dictionary

class MockAuthManager: AuthManager {
    let accounts = ["email@gmail.com": "Password"]
    
    func login(email:String, pwd: String) async throws {
        if(accounts[email] != pwd) {
            throw AuthError.api(message: "Invalid login credentials", errorCode: .invalidCredentials, underlyingData: Data(), underlyingResponse: HTTPURLResponse(url: URL(string: "https://example.com")!, statusCode: 401, httpVersion: nil, headerFields: nil)!)
        }
    }
    
    func signup(email:String, pwd: String, name: String) async throws {
        if(accounts[email] != nil) {
            throw AuthError.api(message: "User already exists", errorCode: .userAlreadyExists, underlyingData: Data(), underlyingResponse: HTTPURLResponse(url: URL(string: "https://example.com")!, statusCode: 401, httpVersion: nil, headerFields: nil)!)
        }
    }
    
    func signout() async throws {}
}
