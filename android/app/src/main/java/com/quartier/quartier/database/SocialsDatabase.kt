package com.quartier.quartier.database

import com.quartier.quartier.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class Socials(
    val id: String,
    val linkedin_url: String?
)

class SocialsDatabase @Inject constructor(){
    suspend fun getUserSocials(id: String): Socials {
        return supabase.from("socials").select(columns = Columns.ALL) {
            filter {
                Socials::id eq id
            }
        }.decodeSingle()
    }

    suspend fun getUserSocialsList() : List<Socials> {
        return supabase.from("socials").select(columns = Columns.ALL).decodeList<Socials>() //RLS returns all friended users
    }

    suspend fun upsertSocials(uid: String, linkedinURL: String) : UploadStatus {
        try {
            supabase.from("socials").upsert(Socials(id = uid, linkedin_url = linkedinURL)) {
                onConflict = "id"
            }
        } catch (e: Exception) { //TODO Better error handling?
            return UploadStatus.Error
        }
        return UploadStatus.Success
    }
}