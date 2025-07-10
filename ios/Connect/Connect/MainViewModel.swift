//
//  MainViewModel.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//

import Foundation

@MainActor
class MainViewModel : ObservableObject {
    @Published var page: ContentPage = .loading
    private var authStateTask: Task<Void, Never>? = nil
    
    init() {
        listenToAuthChanges()
    }
    
    deinit {
        authStateTask?.cancel()
    }
    
    func listenToAuthChanges() {
        authStateTask = Task {
            for await (event, session) in supabase.auth.authStateChanges {
                switch event {
                case .initialSession:
                    if(session?.isExpired ?? true || session?.user == nil) {
                        page = .login
                    } else {
                        page = .main
                    }
                case .signedIn:
                    page = .main
                case .signedOut:
                    page = .login
                case .userDeleted:
                    page = .login
                default:
                    break
                }
            }
        }
    }
}

enum ContentPage {
    case login, loading, main
}
