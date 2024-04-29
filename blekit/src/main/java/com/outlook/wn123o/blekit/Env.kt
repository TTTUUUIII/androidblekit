package com.outlook.wn123o.blekit

import android.app.Application
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.util.Log
import java.util.UUID

internal object Env {

    const val LOG_TAG = "AndroidBleKit"

    lateinit var context: Application

    lateinit var advertiseSettings: AdvertiseSettings
    lateinit var scanSettings: ScanSettings
    lateinit var advertiseData: AdvertiseData

    var logLevel = Log.ERROR

    var expectMtuSize = 185

    val clientChaConfigUuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    val clientChaDescriptor = BluetoothGattDescriptor(
        clientChaConfigUuid,
        BluetoothGattDescriptor.PERMISSION_READ
                or BluetoothGattDescriptor.PERMISSION_WRITE
    )
    lateinit var serviceUuid: UUID
    lateinit var notifyChaUuid: UUID
    lateinit var writeChaUuid: UUID
}