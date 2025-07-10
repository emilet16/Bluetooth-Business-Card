//
//  RegisterView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct RegisterView: View {
    @Environment(\.dismiss) private var dismiss
    
    @StateObject var viewModel = RegisterViewModel()
    @State var email = ""
    @State var emailValid = false
    
    @State var password = ""
    @State var pwdValid = false
    
    @State var name = ""
    @State var nameValid = false
    
    
    var body : some View {
        VStack {
            Text("We're glad to meet you")
            Text("Please sign up")
            
            TextField(text: $name, prompt: Text("Enter your name here")) {
                Text("Name")
            }.onChange(of: name) {
                nameValid = !name.isEmpty
            }
            
            if(!nameValid && !name.isEmpty) {
                Text("Invalid Input").foregroundStyle(.red).font(.caption)
            }
            
            TextField(text: $email, prompt: Text("Enter your email here")) {
                Text("Email")
            }.onChange(of: email) {
                emailValid = !email.isEmpty && matchEmailRegex(text: email)
            }
            
            if(!emailValid && !email.isEmpty) {
                Text("Invalid Input").foregroundStyle(.red).font(.caption)
            }
            
            SecureField(text: $password, prompt: Text("Enter your password here")) {
                Text("Password")
            }.onChange(of: password) {
                pwdValid = !password.isEmpty && password.count >= 6
            }
            
            if(!pwdValid && !password.isEmpty) {
                Text("Invalid Input").foregroundStyle(.red).font(.caption)
            }
            
            Button(action: {
                viewModel.signup(email: email, pwd: password, name: name)
            }) {
                Text("Register")
            }.disabled(!emailValid || !pwdValid || !nameValid)
            
            Button(action: {dismiss()}) {
                Text("Already have an account? Log in")
            }
        }
        .textFieldStyle(RoundedBorderTextFieldStyle())
        .padding()
    }
}

#Preview {
    RegisterView()
}
