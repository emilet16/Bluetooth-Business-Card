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

#if DEBUG
let supabase = SupabaseClient(
  supabaseURL: URL(string: "https://summary-toucan-chief.ngrok-free.app")!, //URL for my local server, TODO: replace for an environment var ideally
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
