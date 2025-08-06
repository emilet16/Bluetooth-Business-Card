//
//  RegisterView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

//Registration screen for the app

struct RegisterView<T: RegisterViewModel>: View {
    @Environment(\.dismiss) private var dismiss
    
    @StateObject var viewModel: T
    @State var email = ""
    @State var emailValid = false
    
    @State var password = ""
    @State var pwdValid = false
    
    @State var name = ""
    @State var nameValid = false
    
    
    var body : some View {
        ZStack(alignment: .bottom) {
            //Show a message if something goes wrong, ex. user already exists
            if let message = viewModel.message {
                Text(message).font(.roboto(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            VStack {
                Text("We're glad to meet you").font(.roboto(24))
                Text("Please sign up").font(.roboto(20))
                
                TextField(text: $name, prompt: Text("Enter your name here")) {
                    Text("Name")
                }.onChange(of: name) {
                    nameValid = !name.isEmpty
                }.font(.roboto(17))
                
                if(!nameValid && !name.isEmpty) {
                    Text("Invalid Input").foregroundStyle(.red).font(.caption)
                }
                
                TextField(text: $email, prompt: Text("Enter your email here")) {
                    Text("Email")
                }.keyboardType(.emailAddress).onChange(of: email) {
                    emailValid = !email.isEmpty && viewModel.matchEmailRegex(text: email)
                }.font(.roboto(17))
                
                if(!emailValid && !email.isEmpty) {
                    Text("Invalid Input").foregroundStyle(.red).font(.caption)
                }
                
                SecureField(text: $password, prompt: Text("Enter your password here")) {
                    Text("Password")
                }.onChange(of: password) {
                    pwdValid = !password.isEmpty && password.count >= 6
                }.font(.roboto(17))
                
                if(!pwdValid && !password.isEmpty) {
                    Text("Invalid Input").foregroundStyle(.red).font(.caption)
                }
                
                Button(action: {
                    viewModel.signup(email: email, pwd: password, name: name)
                }) {
                    Text("Register").foregroundStyle(Color("OnAccentColor")).font(.roboto(17))
                }.disabled(!emailValid || !pwdValid || !nameValid).buttonStyle(.borderedProminent)
                
                Button(action: {dismiss()}) {
                    Text("Already have an account? Log in").font(.roboto(17))
                }
            }
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .padding()
            .frame(maxHeight: .infinity)
        }
    }
}

#Preview {
    RegisterView(viewModel: MockRegisterVM())
}

class MockRegisterVM : RegisterViewModel {
    var message: String? = nil
    func signup(email: String, pwd: String, name: String) {}
    func matchEmailRegex(text: String) -> Bool {true}
}
