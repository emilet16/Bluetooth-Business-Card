//
//  MockSocialsRepo.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-23.
//

import Testing
import Supabase
@testable import Quartier

//Mock the supabase db socials table

class MockSocialsRepo : SocialsRepository, @unchecked Sendable {
    var error = false
    
    private(set) var socials: [Socials] = [
        Socials(id: "0", linkedin_url: "link"),
        Socials(id: "1", linkedin_url: nil),
        Socials(id: "2", linkedin_url: "link"),
        Socials(id: "3", linkedin_url: nil)
    ]
    
    func getUserSocials() async throws -> Socials { //Get user's socials
        return socials[0]
    }
    
    func getConnectedSocials() async throws -> [Socials] { //Mock the behavior of RLS, only returns connected users socials
        let connectionsRepo = MockConnectionsRepo()
        let connections = connectionsRepo.connections
        return socials.filter {
            let uid = $0.id
            let connection = connections.first(where: {
                ($0.requested_by == "0" && $0.requested_for == uid) ||
                ($0.requested_for == "0" && $0.requested_by == uid)
            })
            return connection?.status == "accepted"
        }
    }
    
    func upsertSocials(linkedInUrl: String) async throws {
        if(error) { //Error can be requested by the test
            throw PostgrestError(message: "Error!")
        }
        socials[0] = Socials(id: "0", linkedin_url: linkedInUrl)
    }
}
