package com.quartier.quartier.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quartier.quartier.ImageManager
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsDatabase
import com.quartier.quartier.database.UploadStatus
import com.quartier.quartier.database.User
import com.quartier.quartier.database.UserDatabase
import com.quartier.quartier.supabase
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
class EditProfileViewModel @Inject constructor(private val userDatabase: UserDatabase,
    private val socialsDatabase: SocialsDatabase, private val imageManager: ImageManager) : ViewModel() {
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
            _userState.value = userDatabase.getUser()
        }
        viewModelScope.launch {
            _socials.value = socialsDatabase.getUserSocials()
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
                userResult = userDatabase.updateUser(savedName, savedJob)
            }
            launch {
                pfpResult = if(_newPfpUri.value != null) {
                    val image = imageManager.preparePfpForUpload(_newPfpUri.value!!)
                    userDatabase.uploadPfp(Uuid.random().toString()+".webp", image)
                } else {
                    UploadStatus.Success
                }
            }
            launch { 
                socialsResult = if(savedLinkedin != null) {
                    socialsDatabase.upsertSocials(savedLinkedin)
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