package com.quartier.quartier.profile

import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.User
import com.quartier.quartier.mock_models.MockSocialsRepo
import com.quartier.quartier.mock_models.MockUserRepo
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
class ProfileViewModelTest {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var userRepo: MockUserRepo
    private lateinit var socialsRepo: MockSocialsRepo
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepo = MockUserRepo()
        socialsRepo = MockSocialsRepo()
        viewModel = ProfileViewModel(userRepo, socialsRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshUser() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.refreshUser()
        testScheduler.advanceUntilIdle()

        assertEquals(User("0", "name", "job"), viewModel.uiState.value.user)
        assertEquals(Socials("0", "link"), viewModel.uiState.value.socials)
    }
}