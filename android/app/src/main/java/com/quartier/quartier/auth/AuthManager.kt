package com.quartier.quartier.auth

import com.quartier.quartier.R
import com.quartier.quartier.supabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

//A class handling authentication with supabase

interface AuthManager {
    suspend fun emailSignIn(eml: String, pwd: String): AuthResult
    suspend fun emailSignUp(name: String, eml: String, pwd: String): AuthResult
}

@Singleton
class AuthManagerImpl @Inject constructor() : AuthManager {
    override suspend fun emailSignIn(eml: String, pwd: String) : AuthResult {
        try{
            supabase.auth.signInWith(Email) {
                email = eml
                password = pwd
            }
        } catch(e: AuthRestException) {
            return AuthResult.Error(messageFromErrorCode(e.errorCode ?: AuthErrorCode.UnexpectedFailure))
        } catch(e: HttpRequestException) {
            return AuthResult.Error(R.string.no_internet)
        }
        return AuthResult.Success
    }

    override suspend fun emailSignUp(name: String, eml: String, pwd: String) : AuthResult {
        try {
            supabase.auth.signUpWith(Email) {
                email = eml
                password = pwd
                data = buildJsonObject {
                    put("name", name)
                }
            }
        } catch(e: AuthRestException) {
            return AuthResult.Error(messageFromErrorCode(e.errorCode ?: AuthErrorCode.UnexpectedFailure))
        } catch(e: HttpRequestException) {
            return AuthResult.Error(R.string.no_internet)
        }
        return AuthResult.Success
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class AuthManagerModule {
    @Binds
    abstract fun bindAuthManager(authManagerImpl: AuthManagerImpl): AuthManager
}

interface AuthResult {
    data object Success: AuthResult
    data class Error(val error: Int): AuthResult
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