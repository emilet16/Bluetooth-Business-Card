//
//  RegisterView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct RegisterView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var authManager: AuthManager
    @State var email = ""
    @State var password = ""
    @State var name = ""
    
    var body : some View {
        VStack {
            Text("We're glad to meet you")
            Text("Please sign up")
            TextField(text: $name, prompt: Text("Enter your name here")) {
                Text("Name")
            }
            TextField(text: $email, prompt: Text("Enter your email here")) {
                Text("Email")
            }
            SecureField(text: $password, prompt: Text("Enter your password here")) {
                Text("Password")
            }
            Button(action: {
                authManager.signup(email: email, pwd: password, name: name)
            }) {
                Text("Register")
            }
            Button(action: {dismiss()}) {
                Text("Already have an account? Log in")
            }
        }
        .textFieldStyle(RoundedBorderTextFieldStyle())
        .padding()
        .onChange(of: authManager.userID) {
            dismiss()
        }
    }
}

#Preview {
    RegisterView()
}
