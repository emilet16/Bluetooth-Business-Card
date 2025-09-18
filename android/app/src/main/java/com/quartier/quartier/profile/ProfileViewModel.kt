package com.quartier.quartier.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.ImageRepository
import com.quartier.quartier.R
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsRepository
import com.quartier.quartier.database.SupabaseException
import com.quartier.quartier.database.UploadStatus
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//View model for the profile screen, used to fetch the user's profile

data class ProfileScreenState(
    val user: User?,
    val socials: Socials?,
    val isRefreshing: Boolean = false,
    val userMessage: Int?
)

data class EditProfileState(
    val shown: Boolean = false,
    val newPfpUri: Uri?,
    val saveStatus: UploadStatus?
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val socialsRepository: SocialsRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _socials: MutableStateFlow<Socials?> = MutableStateFlow(null)
    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _userMessage = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ProfileScreenState> = combine(_user, _socials, _isRefreshing, _userMessage) { user, socials, isRefreshing, message ->
        ProfileScreenState(user, socials, isRefreshing, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        ProfileScreenState(null, null, false, null))

    private val _editShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _newPfpUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    private val _saveStatus: MutableStateFlow<UploadStatus?> = MutableStateFlow(null)

    val editState: StateFlow<EditProfileState> = combine(_editShown, _newPfpUri, _saveStatus) { show, uri, status ->
        EditProfileState(show, uri, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        EditProfileState(false, null, null))

    init {
        refreshUser()
    }

    fun refreshUser() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _user.value = userRepository.getUser()
            } catch (e: SupabaseException) {
                _userMessage.value = R.string.no_internet
            } catch (e: HttpRequestException) {
                _userMessage.value = R.string.no_internet
            }
            _isRefreshing.value = false
        }
        viewModelScope.launch {
            try {
                _socials.value = socialsRepository.getUserSocials()
            } catch (e: SupabaseException) {
                _userMessage.value = R.string.no_internet
            } catch (e: HttpRequestException) {
                _userMessage.value = R.string.no_internet
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveUser(newName: String, newJob: String, newLinkedin: String) {
        //If the user didn't change any of these values, keep the current one
        _user.value?.let { user ->
            val savedName = newName.ifBlank { user.name }
            val savedJob = newJob.ifBlank { user.job }
            val savedLinkedin = newLinkedin.ifBlank { _socials.value?.linkedin_url }
            _saveStatus.value = UploadStatus.Loading

            viewModelScope.launch {
                val success = try {
                    coroutineScope {
                        //Save on all tables simultaneously
                        val profileJob = async { userRepository.updateUser(savedName, savedJob) }
                        val pfpJob = async {
                            _newPfpUri.value?.let { uri ->
                                val croppedImage = imageRepository.cropImageTo400(uri = uri) //Crop the pfp to the right format
                                val imageData = imageRepository.convertToWebPByteArray(croppedImage) //Convert to WebP
                                userRepository.uploadPfp(Uuid.random().toString() + ".webp", imageData) //Upload the image
                            }
                        }
                        val socialsJob = async {
                            savedLinkedin?.let { savedLinkedin ->
                                socialsRepository.upsertSocials(savedLinkedin)
                            }
                        }

                        awaitAll(profileJob, pfpJob, socialsJob)
                    }
                    true
                } catch (e: Exception) {
                    _saveStatus.value = UploadStatus.Error
                    false
                } catch (e: HttpRequestException) {
                    _saveStatus.value = UploadStatus.Error
                    false
                }

                if(success) _saveStatus.value = UploadStatus.Success
            }
        }
    }

    //Method to check if the link inserted is a valid linkedin profile
    fun matchesLinkedinRegex(input: String): Boolean {
        val linkedinRegex = Regex("^https://www\\.linkedin\\.com/in/[^/]+/?$")
        return linkedinRegex.matches(input)
    }

    fun previewNewPfp(uri: Uri) {
        _newPfpUri.value = uri
    }

    fun snackbarMessageShown() {
        _saveStatus.value = null
    }

    fun showEdit() {
        _editShown.value = true
    }

    fun hideEdit() {
        _editShown.value = false
    }
}