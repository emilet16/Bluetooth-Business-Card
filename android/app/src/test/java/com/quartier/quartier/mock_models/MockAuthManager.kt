package com.quartier.quartier.mock_models

import com.quartier.quartier.R
import com.quartier.quartier.auth.AuthManager
import com.quartier.quartier.auth.AuthResult

//Imitate the behavior of supabase authentication

class MockAuthManager : AuthManager {
    val accounts = mutableMapOf(
        Pair("email@gmail.com", "Password")
    )

    override suspend fun emailSignIn(
        eml: String,
        pwd: String
    ): AuthResult {
        return if(accounts[eml] == pwd) AuthResult.Success
        else AuthResult.Error(R.string.auth_invalid_credentials)
    }

    override suspend fun emailSignUp(
        name: String,
        eml: String,
        pwd: String
    ): AuthResult {
        return if(accounts[eml] != null) AuthResult.Error(R.string.auth_user_exists)
        else AuthResult.Success
    }
}