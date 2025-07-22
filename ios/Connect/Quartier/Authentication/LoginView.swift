//
//  LoginView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct LoginView : View {
    @StateObject var viewModel = LoginViewModel()
    @State var email = ""
    @State var emailValid = false
    
    @State var password = ""
    @State var pwdValid = false
    
    var body : some View {
        ZStack(alignment: .bottom) {
            if let message = viewModel.message {
                Text(message).font(.roboto(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            VStack {
                Text("Welcome!").font(.roboto(24))
                Text("Please login").font(.roboto(20))
                
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
                    viewModel.login(email: email, pwd: password)
                }) {
                    Text("Login").foregroundStyle(Color("OnAccentColor")).font(.roboto(17))
                }.disabled(!emailValid || !pwdValid).buttonStyle(.borderedProminent)
                
                NavigationLink(destination: RegisterView()) {
                    Text("Don't have an account? Register here.").font(.roboto(17))
                }
            }
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .padding()
            .frame(maxHeight: .infinity)
        }
    }
}

#Preview {
    LoginView()
}
