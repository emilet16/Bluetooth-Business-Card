//
//  SocialsDatabase.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-12.
//

struct Socials: Decodable, Hashable {
    var id: String
    var linkedin_url: String?
}

class SocialsDatabase {
    static let shared = SocialsDatabase()
    
    func getUserSocials() async throws -> Socials {
        let userID = supabase.auth.currentUser!.id.uuidString
        return try await (supabase.from("socials").select("*").eq("id", value: userID).execute().value as [Socials]).first!
    }
    
    func getConnectedSocials() async throws -> [Socials] {
        return try await (supabase.from("socials").select("*").execute().value as [Socials])
    }
    
    func upsertSocials(linkedInUrl: String) async throws {
        let userID = supabase.auth.currentUser!.id.uuidString
        try await supabase.from("socials").upsert(["id": userID, "linkedin_url": linkedInUrl], onConflict: "id").execute()
    }
}
