package com.example.businesscard.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscard.Socials
import com.example.businesscard.UploadStatus
import com.example.businesscard.User
import com.example.businesscard.UserRepository
import com.example.businesscard.supabase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
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

    private val userId: String = supabase.auth.currentUserOrNull()!!.id

    init {
        viewModelScope.launch {
            _userState.value = userRepository.getUser(userId)
        }
        viewModelScope.launch {
            _socials.value = userRepository.getUserSocials(userId)
        }
    }

    fun saveUser(newName: String, newJob: String, newLinkedin: String) {
        val savedName = newName.ifBlank { _userState.value!!.name }
        val savedJob = newJob.ifBlank { _userState.value!!.job }
        val savedLinkedin = newLinkedin.ifBlank { _socials.value?.linkedin_url }
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
                userResult = userRepository.updateUser(userId, savedName, savedJob)
            }
            launch {
                pfpResult = if(_newPfpUri.value != null) {
                    userRepository.uploadPfp(userId,Uuid.random().toString()+".webp", _newPfpUri.value!!)
                } else {
                    UploadStatus.Success
                }
            }
            launch { 
                socialsResult = if(savedLinkedin != null) {
                    userRepository.upsertSocials(userId, savedLinkedin)
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