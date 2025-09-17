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

//Viewmodel tests for the profile screen

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileViewModelTest {
    private lateinit var viewModel: ProfileViewModel
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
        viewModel = ProfileViewModel(userRepo, socialsRepo, imageRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshUser() = runTest { //Ensure user profile is loaded properly
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.refreshUser()
        testScheduler.advanceUntilIdle()

        assertEquals(User("0", "name", "job"), viewModel.uiState.value.user)
        assertEquals(Socials("0", "link"), viewModel.uiState.value.socials)
    }

    @Test
    fun saveUser_success() = runTest { //Save user, normal case
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("newname", "newjob", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.editState.value.saveStatus)
        assertEquals(Socials("0", "linkedin"), socialsRepo.getUserSocials())
        assertEquals("newname", userRepo.getUser().name)
        assertEquals("newjob", userRepo.getUser().job)
        assertEquals("https://google.com", userRepo.getUser().pfp_url)
    }

    @Test
    fun saveUser_socialsError() = runTest { //Save user, error saving socials
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        socialsRepo.error = true //simulate error and make sure it shows
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Error, viewModel.editState.value.saveStatus)
    }

    @Test
    fun saveUser_profileError() = runTest { //Save user, error saving profile details
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        userRepo.profileError = true //simulate error and make sure it shows
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Error, viewModel.editState.value.saveStatus)
    }

    @Test
    fun saveUser_pfpError() = runTest { //Save user, error saving pfp
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        userRepo.pfpError = true //simulate error and make sure it shows
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Error, viewModel.editState.value.saveStatus)
    }

    @Test
    fun saveUser_noChangeSocials() = runTest { //Save user, but don't change socials
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("name", "job", "")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.editState.value.saveStatus)
        assertEquals(Socials("0", "link"), socialsRepo.getUserSocials())
    }

    @Test
    fun saveUser_noChangePfp() = runTest { //Save user but don't change pfp
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        viewModel.saveUser("name", "job", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.editState.value.saveStatus)
        assertEquals(null, userRepo.getUser().pfp_url)
    }

    @Test
    fun saveUser_noChangeProfile() = runTest { //Save user but don't change profile details
        backgroundScope.launch {
            viewModel.editState.collect()
        }
        viewModel.previewNewPfp(Uri.parse("https://google.com"))
        viewModel.saveUser("", "", "linkedin")
        testScheduler.advanceUntilIdle()
        assertEquals(UploadStatus.Success, viewModel.editState.value.saveStatus)
        assertEquals("name", userRepo.getUser().name)
        assertEquals("job", userRepo.getUser().job)
    }

    @Test
    fun linkedinRegex_valid() = runTest { //Valid linkedin
        val linkedin = "https://www.linkedin.com/in/user"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), true)
    }

    @Test
    fun linkedinRegex_google() = runTest { //Not linkedin link
        val linkedin = "https://www.google.com/search?query=query"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), false)
    }

    @Test
    fun linkedinRegex_invalidLink() = runTest { //Missing https, could change in the future
        val linkedin = "www.linkedin.com/in/user"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), false)
    }

    @Test
    fun linkedinRegex_noUser() = runTest { //Missing username
        val linkedin = "https://www.linkedin.com/in/"
        assertEquals(viewModel.matchesLinkedinRegex(linkedin), false)
    }
}