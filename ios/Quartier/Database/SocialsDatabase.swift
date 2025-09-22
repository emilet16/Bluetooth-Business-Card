//
//  SocialsDatabase.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-12.
//

//A class handling methods for the Socials table in supabase DB

struct Socials: Decodable, Hashable {
    var id: String
    var linkedin_url: String?
}

protocol SocialsRepository : Sendable {
    func getUserSocials() async throws -> Socials?
    func getConnectedSocials() async throws -> [Socials]
    func upsertSocials(linkedInUrl: String) async throws
}

final class SocialsDatabase : SocialsRepository {
    static let shared = SocialsDatabase()
    
    func getUserSocials() async throws -> Socials? { //Get the current user's socials
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        return try await (supabase.from("socials").select("*").eq("id", value: userID).execute().value as [Socials]).first
    }
    
    func getConnectedSocials() async throws -> [Socials] { //Get socials of all connected users, RLS only allows reading connected users, so no filtering needed
        return try await (supabase.from("socials").select("*").execute().value as [Socials])
    }
    
    func upsertSocials(linkedInUrl: String) async throws {
        let userID = supabase.auth.currentUser?.id.uuidString
        guard let userID else {
            throw SupabaseError.authError("Error: No user logged in")
        }
        
        try await supabase.from("socials").upsert(["id": userID, "linkedin_url": linkedInUrl], onConflict: "id").execute()
    }
}
