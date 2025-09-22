//
//  ConnectApp.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-17.
//

import SwiftUI
import Supabase
import SDWebImage
import SDWebImageWebPCoder

//Entry point for the app, load some libraries

#if LOCAL
let supabase = SupabaseClient(
  supabaseURL: URL(string: "http://localhost:54321")!, //URL for my local server, use localhost for testing
  supabaseKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"
)
#elseif DEBUG
let supabase = SupabaseClient(
  supabaseURL: URL(string: "https://summary-toucan-chief.ngrok-free.app")!, //URL for my local server
  supabaseKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"
)
#else
let supabase = SupabaseClient(
  supabaseURL: URL(string: "https://liumhaenwcmzwargxnpv.supabase.co")!,
  supabaseKey: "sb_publishable_YVhSNGzOosROZp8bY-qzPQ_xJafBP1M"
)
#endif

@main
struct QuartierApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        let WebPCoder = SDImageWebPCoder.shared
        SDImageCodersManager.shared.addCoder(WebPCoder)
        
        return true
    }
}
