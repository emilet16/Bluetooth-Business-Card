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

let supabase = SupabaseClient(
  supabaseURL: URL(string: "https://liumhaenwcmzwargxnpv.supabase.co")!,
  supabaseKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpdW1oYWVud2Ntendhcmd4bnB2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkwNjIzNDUsImV4cCI6MjA2NDYzODM0NX0.UtzshBOjki83CdLOFu1dyu70JKDQOgcaHtxo1WqrpwY"
)

@main
struct ConnectApp: App {
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
