package com.quartier.quartier.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.ImageRepository
import com.quartier.quartier.R
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsRepository
import com.quartier.quartier.database.UploadStatus
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//View model for the Edit Profile screen
//Used to fetch profile and save changes

data class EditProfileUIState(
    val newPfpUri: Uri? = null,
    val user: User? = null,
    val saveStatus: UploadStatus? = null,
    val socials: Socials? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val socialsRepository: SocialsRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val _newPfpUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    private val _userState: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _saveStatus: MutableStateFlow<UploadStatus?> = MutableStateFlow(null)
    private val _socials: MutableStateFlow<Socials?> = MutableStateFlow(null)

    val uiState = combine(_newPfpUri, _userState, _saveStatus, _socials) { pfpUri, user, status, socials ->
        EditProfileUIState(pfpUri, user, status, socials)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditProfileUIState())

    init {
        viewModelScope.launch {
            _userState.value = userRepository.getUser()
        }
        viewModelScope.launch {
            _socials.value = socialsRepository.getUserSocials()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveUser(newName: String, newJob: String, newLinkedin: String) {
        //If the user didn't change any of these values, keep the current one
        val savedName = newName.ifBlank { _userState.value!!.name }
        val savedJob = newJob.ifBlank { _userState.value!!.job }
        val savedLinkedin = newLinkedin.ifBlank { _socials.value?.linkedin_url }
        _saveStatus.value = UploadStatus.Loading

        viewModelScope.launch {
            val success = try {
                coroutineScope {
                    //Save on all tables simultaneously
                    val profileJob = async { userRepository.updateUser(savedName, savedJob) }
                    val pfpJob = async {
                        if(_newPfpUri.value != null) {
                            val croppedImage = imageRepository.cropImageTo400(uri = _newPfpUri.value!!) //Crop the pfp to the right format
                            val imageData = imageRepository.convertToWebPByteArray(croppedImage) //Convert to WebP
                            userRepository.uploadPfp(Uuid.random().toString() + ".webp", imageData) //Upload the image
                        }
                    }
                    val socialsJob = async {
                        if(savedLinkedin != null) {
                            socialsRepository.upsertSocials(savedLinkedin)
                        }
                    }

                    awaitAll(profileJob, pfpJob, socialsJob)
                }
                true
            } catch (e: Exception) {
                _saveStatus.value = UploadStatus.Error
                false
            }

            if(success) _saveStatus.value = UploadStatus.Success
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
}