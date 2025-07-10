package com.example.businesscard.auth

import com.example.businesscard.R
import com.example.businesscard.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class AuthManager @Inject constructor() {
    val auth = supabase.auth

    suspend fun emailSignIn(eml: String, pwd: String) : AuthResult {
        try{
            auth.signInWith(Email) {
                email = eml
                password = pwd
            }
        } catch(e: AuthRestException) {
            return AuthResult.Error(e.errorCode ?: AuthErrorCode.UnexpectedFailure)
        }
        return AuthResult.Success
    }

    suspend fun emailSignUp(name: String, eml: String, pwd: String) : AuthResult {
        try {
            auth.signUpWith(Email) {
                email = eml
                password = pwd
                data = buildJsonObject {
                    put("name", name)
                }
            }
        } catch(e: AuthRestException) {
            return AuthResult.Error(e.errorCode ?: AuthErrorCode.UnexpectedFailure)
        }
        return AuthResult.Success
    }
}

interface AuthResult {
    data object Success: AuthResult
    data class Error(val error: AuthErrorCode): AuthResult
}

fun messageFromErrorCode(errorCode: AuthErrorCode) : Int {
    val message = when(errorCode) {
        AuthErrorCode.EmailExists -> R.string.auth_email_exists
        AuthErrorCode.UserNotFound -> R.string.auth_user_not_found
        AuthErrorCode.UserBanned -> R.string.auth_user_banned
        AuthErrorCode.UserAlreadyExists -> R.string.auth_user_exists
        AuthErrorCode.ProviderDisabled -> R.string.auth_provider_disabled
        AuthErrorCode.WeakPassword -> R.string.auth_weak_password
        AuthErrorCode.InvalidCredentials -> R.string.auth_invalid_credentials
        AuthErrorCode.RequestTimeout -> R.string.auth_timeout
        AuthErrorCode.OtpDisabled -> R.string.auth_user_not_found
        AuthErrorCode.OtpExpired -> R.string.auth_otp_expired
        else -> R.string.unexpected_error
    }
    return message
}