package com.quartier.quartier.profile

import android.net.Uri
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.UploadStatus
import com.quartier.quartier.database.User
import com.quartier.quartier.mock_models.MockImageRepo
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URI

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class EditProfileViewModelTest {
    private lateinit var viewModel: EditProfileViewModel
    private lateinit var userRepo: MockUserRepo
    private lateinit var socialsRepo: MockSocialsRepo
    private lateinit var imageRepo: MockImageRepo
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepo = MockUserRepo()
        socialsRepo = MockSocialsRepo()
        imageRepo = MockImageRepo()
        viewModel = EditProfileViewModel(userRepo, socialsRepo, imageRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveUser_success() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("newname", "newjob", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.uiState.value.saveStatus)
        assertEquals(Socials("0", "linkedin"), socialsRepo.getUserSocials())
        assertEquals("newname", userRepo.getUser().name)
        assertEquals("newjob", userRepo.getUser().job)
        assertEquals("https://google.com", userRepo.getUser().pfp_url)
    }

    @Test
    fun saveUser_socialsError() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        socialsRepo.error = true //simulate error and make sure it shows
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Error, viewModel.uiState.value.saveStatus)
    }

    @Test
    fun saveUser_profileError() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        userRepo.profileError = true //simulate error and make sure it shows
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Error, viewModel.uiState.value.saveStatus)
    }

    @Test
    fun saveUser_pfpError() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        userRepo.pfpError = true //simulate error and make sure it shows
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Error, viewModel.uiState.value.saveStatus)
    }

    @Test
    fun saveUser_noChangeSocials() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.uiState.value.saveStatus)
        assertEquals(Socials("0", "link"), socialsRepo.getUserSocials())
    }

    @Test
    fun saveUser_noChangePfp() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.uiState.value.saveStatus)
        assertEquals(null, userRepo.getUser().pfp_url)
    }

    @Test
    fun saveUser_noChangeProfile() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("", "", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.uiState.value.saveStatus)
        assertEquals("name", userRepo.getUser().name)
        assertEquals("job", userRepo.getUser().job)
    }

    @Test
    fun linkedinRegex_valid() = runTest {
        val linkedin = "https://www.linkedin.com/in/user"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), true)
    }

    @Test
    fun linkedinRegex_google() = runTest {
        val linkedin = "https://www.google.com/search?query=query"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), false)
    }

    @Test
    fun linkedinRegex_invalidLink() = runTest {
        val linkedin = "www.linkedin.com/in/user"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), false)
    }

    @Test
    fun linkedinRegex_noUser() = runTest {
        val linkedin = "https://www.linkedin.com/in/"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), false)
    }
}