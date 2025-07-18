package com.quartier.quartier.mock_models

import com.quartier.quartier.database.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthRepo() : AuthRepository {
    private val _userId = MutableStateFlow<String?>("0")
    override val userId: StateFlow<String?> = _userId.asStateFlow()

    override fun updateUserId(newUserId: String) {
        _userId.value = newUserId
    }
}