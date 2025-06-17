package com.example.businesscard.ble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.businesscard.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleServices @Inject constructor(@ApplicationContext val context: Context) {
    private val bleUUID: String = "0000D17B-0000-1000-8000-00805F9B34FB"

    private val _userIds: MutableStateFlow<List<String>> = MutableStateFlow<List<String>>(listOf<String>())
    val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    val advertisingSetCallback = object : AdvertisingSetCallback() {
        override fun onAdvertisingSetStarted(
            advertisingSet: AdvertisingSet?,
            txPower: Int,
            status: Int
        ) {
            super.onAdvertisingSetStarted(advertisingSet, txPower, status)
            Log.i(TAG, "Advertising Started, STATUS: $status")
        }

        override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet?, status: Int) {
            super.onAdvertisingDataSet(advertisingSet, status)
            Log.i(TAG, "Advertising, STATUS: $status")
        }

        override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
            super.onAdvertisingSetStopped(advertisingSet)
            Log.i(TAG, "Advertising Stopped")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if(result != null && result.scanRecord != null) {
                val userId = result.scanRecord?.serviceData[result.scanRecord?.serviceUuids[0]]
                if(userId != null) {
                    val idString: String = userId.toString(Charsets.UTF_8)
                    if(!_userIds.value.contains(idString)) {
                        _userIds.value = _userIds.value.plus(idString)
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun startAdvertising(data: ByteArray) {
        val pUuid = ParcelUuid.fromString(bleUUID)

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

        val parameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setConnectable(false)
            .setInterval(AdvertisingSetParameters.INTERVAL_MEDIUM)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(false)
            .setIncludeDeviceName(false)
            .addServiceUuid(pUuid)
            .addServiceData(pUuid, data)
            .build()

        advertiser.startAdvertisingSet(parameters, data, null, null, null, advertisingSetCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun stopAdvertising() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

        advertiser.stopAdvertisingSet(advertisingSetCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScanning(){
        val pUuid = ParcelUuid.fromString(bleUUID)

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        val filter = ScanFilter.Builder()
            .setServiceUuid(pUuid)
            .build()

        _userIds.value = emptyList<String>()

        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScanning() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter.bluetoothLeScanner

        scanner.stopScan(scanCallback)
    }
}