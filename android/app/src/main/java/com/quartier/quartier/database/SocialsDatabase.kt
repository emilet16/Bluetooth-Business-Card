package com.quartier.quartier.database

import com.quartier.quartier.supabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

//Class to interface with the Socials table in the supabase database

@Serializable
data class Socials(
    val id: String,
    val linkedin_url: String?
)

interface SocialsRepository {
    suspend fun getUserSocials(): Socials
    suspend fun getUserSocialsList(): List<Socials>
    suspend fun upsertSocials(linkedinURL: String)
}

@Singleton
class SocialsDatabase @Inject constructor(private val authRepository: AuthRepository) : SocialsRepository {
    override suspend fun getUserSocials(): Socials {
        val id = authRepository.userId.value!!
        return supabase.from("socials").select(columns = Columns.ALL) {
            filter {
                Socials::id eq id
            }
        }.decodeSingle()
    }

    override suspend fun getUserSocialsList() : List<Socials> {
        return supabase.from("socials").select(columns = Columns.ALL).decodeList<Socials>() //RLS returns all connected users
    }

    override suspend fun upsertSocials(linkedinURL: String) {
        val uid = authRepository.userId.value!!
        supabase.from("socials").upsert(Socials(id = uid, linkedin_url = linkedinURL)) {
            onConflict = "id" //When modifying, overwrite based on the user ID.
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class SocialsModule {
    @Binds
    abstract fun bindSocialsRepository(socialsDatabase: SocialsDatabase): SocialsRepository
}