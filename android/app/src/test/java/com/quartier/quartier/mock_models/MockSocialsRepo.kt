package com.quartier.quartier.mock_models

import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsRepository
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.ktor.client.statement.HttpResponse

class MockSocialsRepo : SocialsRepository {
    var error = false
    private val socials: MutableList<Socials> = mutableListOf(
        Socials("0", "link"),
        Socials("1", null),
        Socials("2", "link"),
        Socials("3", null)
    )

    override suspend fun getUserSocials(): Socials {
        return socials[0]
    }

    override suspend fun getUserSocialsList(): List<Socials> {
        return listOf(socials[2])
    }

    override suspend fun upsertSocials(linkedinURL: String) {
        if(error) {
            throw Exception()
        }
        socials[0] = Socials("0", linkedinURL)
    }
}