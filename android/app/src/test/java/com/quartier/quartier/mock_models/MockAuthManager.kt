package com.quartier.quartier.mock_models

import com.quartier.quartier.auth.AuthManager
import com.quartier.quartier.auth.AuthResult
import io.github.jan.supabase.auth.exception.AuthErrorCode

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
        else AuthResult.Error(AuthErrorCode.InvalidCredentials)
    }

    override suspend fun emailSignUp(
        name: String,
        eml: String,
        pwd: String
    ): AuthResult {
        return if(accounts[eml] != null) AuthResult.Error(AuthErrorCode.UserAlreadyExists)
        else AuthResult.Success
    }
}