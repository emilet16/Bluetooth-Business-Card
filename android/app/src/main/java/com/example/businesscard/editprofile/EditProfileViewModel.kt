package com.example.businesscard.editprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.supabase.Socials
import com.example.businesscard.supabase.UploadStatus
import com.example.businesscard.supabase.User
import com.example.businesscard.supabase.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditProfileUIState(
    val newPfpUri: Uri? = null,
    val user: User? = null,
    val userMessage: Int? = null,
    val saveStatus: UploadStatus? = null,
    val socials: Socials? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {
    private val _newPfpUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    private val _userState: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _saveStatus: MutableStateFlow<UploadStatus?> = MutableStateFlow(null)
    private val _socials: MutableStateFlow<Socials?> = MutableStateFlow(null)

    val uiState = combine(_newPfpUri, _userState, _userMessage, _saveStatus, _socials) { pfpUri, user, message, status, socials ->
        EditProfileUIState(pfpUri, user, message, status, socials)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditProfileUIState())

    init {
        viewModelScope.launch {
            _userState.value = userRepository.getCurrentUser()
            _socials.value = userRepository.getUserSocials(_userState.value!!.id)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveUser(newName: String, newJob: String, newLinkedin: String) {
        val savedName = if(newName.isNotBlank()) newName else _userState.value!!.name
        val savedJob = if(newJob.isNotBlank()) newJob else _userState.value!!.job
        val savedLinkedin = if(newLinkedin.isNotBlank()) newLinkedin else _socials.value?.linkedin_url
        _saveStatus.value = UploadStatus.Loading

        viewModelScope.launch {
            _saveStatus.value = saveConcurrently(savedName, savedJob, savedLinkedin)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun saveConcurrently(savedName: String, savedJob: String, savedLinkedin: String?) : UploadStatus {
        var userResult: UploadStatus = UploadStatus.Loading
        var pfpResult: UploadStatus = UploadStatus.Loading
        var socialsResult: UploadStatus = UploadStatus.Loading

        coroutineScope {
            launch {
                userResult = userRepository.updateUser(savedName, savedJob)
            }
            launch {
                pfpResult = if(_newPfpUri.value != null) {
                    userRepository.uploadPfp(Uuid.random().toString()+".webp", _newPfpUri.value!!)
                } else {
                    UploadStatus.Success
                }
            }
            launch { 
                socialsResult = if(savedLinkedin != null) {
                    userRepository.upsertSocials(savedLinkedin)
                } else {
                    UploadStatus.Success
                }
            }
        }

        return if(userResult == UploadStatus.Success && pfpResult == UploadStatus.Success && socialsResult == UploadStatus.Success) {
            UploadStatus.Success
        } else {
            UploadStatus.Error
        }
    }

    fun previewNewPfp(uri: Uri) {
        _newPfpUri.value = uri
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }
}