package com.quartier.quartier.mock_models

import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserRepository

//Imitate the profiles table in supabase, with different users as test cases

class MockUserRepo : UserRepository {
    var profileError = false
    var pfpError = false
    private val users: MutableList<User> = mutableListOf(
        User("0", "name", "job"),
        User("1", "name", "job"),
        User("2", "name", "job"),
        User("3", "name", "job"),
        User("4", "name", "job"),
        User("5", "name", "job"),
    )

    override suspend fun getUsers(uids: List<String>): List<User> {
        return users.filter { uids.contains(it.id) }
    }

    override suspend fun getUser(): User {
        return users[0]
    }

    override suspend fun updateUser(
        name: String,
        job: String
    ) {
        if(profileError) {
            throw Exception()
        }
        users[0] = User("0", name, job, users[0].pfp_url)
    }

    override suspend fun uploadPfp(
        fileName: String,
        image: ByteArray //bytearray containing uri string
    ) {
        if(pfpError) {
            throw Exception()
        }
        val path = image.toString(Charsets.UTF_8)
        users[0] = User("0", users[0].name, users[0].job, path)
    }
}