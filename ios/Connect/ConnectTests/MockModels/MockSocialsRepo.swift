//
//  MockSocialsRepo.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
import Supabase
@testable import Quartier

class MockSocialsRepo : SocialsRepository {
    var error = false
    
    private(set) var socials: [Socials] = [
        Socials(id: "0", linkedin_url: "link"),
        Socials(id: "1", linkedin_url: nil),
        Socials(id: "2", linkedin_url: "link"),
        Socials(id: "3", linkedin_url: nil)
    ]
    
    func getUserSocials() async throws -> Socials {
        return socials[0]
    }
    
    func getConnectedSocials() async throws -> [Socials] {
        return socials.filter { $0.linkedin_url != nil && $0.id != "0" }
    }
    
    func upsertSocials(linkedInUrl: String) async throws {
        if(error) {
            throw PostgrestError(message: "Error!")
        }
        socials[0] = Socials(id: "0", linkedin_url: linkedInUrl)
    }
}
