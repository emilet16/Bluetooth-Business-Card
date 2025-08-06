
package com.quartier.quartier.auth

import com.quartier.quartier.mock_models.MockAuthManager
import io.github.jan.supabase.auth.exception.AuthErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private lateinit var authManager: MockAuthManager
    private lateinit var viewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authManager = MockAuthManager()
        viewModel = RegisterViewModel(authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emailSignUp_valid() = runTest { //Normal registration
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.emailSignUp("name", "newemail@gmail.com", "Password")
        testScheduler.advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value)
    }

    @Test
    fun emailSignUp_alreadyExists() = runTest { //User already exists
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.emailSignUp("name", "email@gmail.com", "Password")
        testScheduler.advanceUntilIdle()

        assertEquals(messageFromErrorCode(AuthErrorCode.UserAlreadyExists), viewModel.uiState.value)
    }

    @Test
    fun emailRegex_valid() = runTest { //Valid email
        val email = "email@gmail.com"
        assertEquals(viewModel.matchesEmailRegex(email), true)
    }

    @Test
    fun emailRegex_invalidEnd() = runTest { //Missing TLD
        val email = "email@gmail"
        assertEquals(viewModel.matchesEmailRegex(email), false)
    }

    @Test
    fun emailRegex_invalidStart() = runTest { //Extra @
        val email = "em@ail@gmail.com"
        assertEquals(viewModel.matchesEmailRegex(email), false)
    }

    @Test
    fun emailRegex_invalidEmail() = runTest { //Missing @
        val email = "emailgmail.com"
        assertEquals(viewModel.matchesEmailRegex(email), false)
    }
}