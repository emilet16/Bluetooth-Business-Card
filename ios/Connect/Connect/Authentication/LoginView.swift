//
//  LoginView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct LoginView : View {
    @EnvironmentObject var authManager: AuthManager
    @State var email = ""
    @State var password = ""
    
    var body : some View {
        VStack {
            Text("Welcome!")
            Text("Please login")
            TextField(text: $email, prompt: Text("Enter your email here")) {
                Text("Email")
            }
            SecureField(text: $password, prompt: Text("Enter your password here")) {
                Text("Password")
            }
            Button(action: {
                authManager.login(email: email, pwd: password)
            }) {
                Text("Login")
            }
            NavigationLink(destination: RegisterView()) {
                Text("Don't have an account? Register here.")
            }
        }
        .textFieldStyle(RoundedBorderTextFieldStyle())
        .padding()
    }
}

#Preview {
    LoginView()
}
