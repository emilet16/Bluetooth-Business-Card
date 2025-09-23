//
//  LoginView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

//Login screen for the app

struct LoginView<T: LoginViewModel> : View {
    @StateObject var viewModel: T
    @State var email = ""
    @State var emailValid = false
    
    @State var password = ""
    @State var pwdValid = false
    
    var body : some View {
        ZStack(alignment: .bottom) {
            //Show a message to the user if something goes wrong (ex. wrong password)
            if let message = viewModel.message {
                Text(message).font(.body(17)).padding().frame(width: 400)
                    .background(Color.cyan.opacity(0.6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            VStack(alignment: .leading) {
                Text("Welcome back!").font(.title(24))
                Text("Please sign in").font(.titleBold(24))
                
                Spacer().frame(height: 15)
                
                TextField(text: $email, prompt: Text("Email")) {
                    Text("Email")
                }.keyboardType(.emailAddress).onChange(of: email) {
                    emailValid = !email.isEmpty && viewModel.matchEmailRegex(text: email)
                }.font(.body(17))
                
                if(!emailValid && !email.isEmpty) {
                    Text("Invalid Input").foregroundStyle(.red).font(.caption)
                }
                
                SecureField(text: $password, prompt: Text("Password")) {
                    Text("Password")
                }.onChange(of: password) {
                    pwdValid = !password.isEmpty && password.count >= 8
                }.font(.body(17))
                
                Spacer().frame(height: 15)
                
                if(!pwdValid && !password.isEmpty) {
                    Text("Password must be 8 characters long").foregroundStyle(.red).font(.caption)
                }
                
                Button(action: {
                    viewModel.login(email: email, pwd: password)
                }) {
                    HStack {
                        Spacer()
                        Text("Login").foregroundStyle(Color("OnAccentColor")).font(.body(17))
                        Spacer()
                    }
                }.disabled(!emailValid || !pwdValid).buttonStyle(.borderedProminent)
                
                NavigationLink(destination: RegisterView(viewModel: RegisterViewModelImpl())) {
                    HStack {
                        Spacer()
                        Text("Don't have an account? Register here.").font(.body(17))
                        Spacer()
                    }
                }
            }
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .padding(20)
            .frame(maxHeight: .infinity)
        }
    }
}

#Preview {
    LoginView(viewModel: MockLoginVM())
}

class MockLoginVM : LoginViewModel {
    var message: String? = nil
    
    func login(email: String, pwd: String) {}
    func matchEmailRegex(text: String) -> Bool {return true}
}
