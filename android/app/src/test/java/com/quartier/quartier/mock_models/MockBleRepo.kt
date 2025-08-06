package com.quartier.quartier.mock_models

import com.quartier.quartier.BleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

//Imitate the ble repo behavior

class MockBleRepo() : BleRepository {
    private val _userIds: MutableStateFlow<List<String>> = MutableStateFlow<List<String>>(emptyList())
    override val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    override fun startAdvertising() {

    }

    override fun stopAdvertising() {

    }

    override fun startScanning() {

    }

    override fun stopScanning() {

    }

    //Allow tests to add uids to their will
    fun setUserIds(ids: List<String>) {
        _userIds.value = ids
    }
}