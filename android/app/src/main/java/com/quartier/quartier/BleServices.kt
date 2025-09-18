package com.quartier.quartier

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
import com.quartier.quartier.database.SupabaseException
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

//Helper class to handle Bluetooth methods/events

interface BleRepository {
    val userIds: StateFlow<List<String>>
    fun startAdvertising()
    fun stopAdvertising()
    fun startScanning()
    fun stopScanning()
}

@Singleton
class BleManager @Inject constructor(@param: ApplicationContext val context: Context) : BleRepository {
    //Service uuid to filter out other bluetooth devices
    private val bleUUID: String = "0000D17B-0000-1000-8000-00805F9B34FB"

    private val _userIds: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    override val userIds: StateFlow<List<String>> = _userIds.asStateFlow()

    private val advertisingSetCallback = object : AdvertisingSetCallback() {
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
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            val serviceIds = result?.scanRecord?.serviceUuids
            //Workaround to make the app work with ios devices, the packet sends 2 service uuids, the actual one, and the user ID.
            //Remove the known service uuid from the packet to keep the "useful" user ID. (Cannot index because iOS has a diff order than Android)
            serviceIds?.remove(ParcelUuid.fromString(bleUUID))

            val userId = serviceIds?.get(0)

            if(userId != null) {
                val idString: String = userId.uuid.toString()
                if(!_userIds.value.contains(idString)) {
                    _userIds.value = _userIds.value.plus(idString)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun startAdvertising() {
        val userID = supabase.auth.currentUserOrNull()?.id

        if(userID == null) throw SupabaseException("No internet connection!")

        val pUuid = ParcelUuid.fromString(bleUUID)

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

        //Enable legacy mode for compatibility with iOS
        val parameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(true)
            .build()

        //Workaround to make the app work with ios devices, the packet sends 2 service uuids, the actual one, and the user ID.
        val advData = AdvertiseData.Builder()
            .addServiceUuid(pUuid)
            .addServiceUuid(ParcelUuid.fromString(userID))
            .build()

        advertiser.startAdvertisingSet(parameters, advData, null, null, null, advertisingSetCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun stopAdvertising() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

        advertiser.stopAdvertisingSet(advertisingSetCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startScanning(){
        val pUuid = ParcelUuid.fromString(bleUUID)

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setLegacy(true)
            .build()

        //Filter the packets to only get the wanted ble devices
        val filter = ScanFilter.Builder()
            .setServiceUuid(pUuid)
            .build()

        _userIds.value = emptyList()

        //When the scanner finds a device matching the filter, it calls the scanCallback method
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopScanning() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter.bluetoothLeScanner

        scanner.stopScan(scanCallback)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class BleModule {
    @Binds
    abstract fun bindBleRepository(bleManager: BleManager): BleRepository
}