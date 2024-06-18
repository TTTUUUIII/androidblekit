package com.outlook.wn123o.blekit

import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import java.util.UUID

internal object BleEnv {

    const val LOG_TAG = "AndroidBleKit"

    lateinit var applicationContext: Context

    lateinit var advertiseSettings: AdvertiseSettings
    lateinit var scanSettings: ScanSettings
    var scanFeatureOnlyReportOnce: Boolean = true

    var logLevel = Log.ERROR

    var expectMtuSize = 185

    val clientChaConfigUuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    val clientChaDescriptor = BluetoothGattDescriptor(
        clientChaConfigUuid,
        BluetoothGattDescriptor.PERMISSION_READ
                or BluetoothGattDescriptor.PERMISSION_WRITE
    )
    lateinit var preferenceServiceUuid: UUID
    lateinit var preferenceNotifyChaUuid: UUID
    lateinit var preferenceWriteChaUuid: UUID
}