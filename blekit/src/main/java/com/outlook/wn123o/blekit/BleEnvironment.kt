package com.outlook.wn123o.blekit

import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.outlook.wn123o.blekit.common.BleGattService
import com.outlook.wn123o.blekit.common.BleKitOptions
import java.util.Objects
import java.util.UUID

object BleEnvironment {

    internal const val LOG_TAG = "AndroidBleKit"

    internal lateinit var applicationContext: Context

    internal lateinit var advertiseSettings: AdvertiseSettings
    internal var advertiseFeatureIncludeDeviceName: Boolean = false
    internal lateinit var scanSettings: ScanSettings
    internal var scanFeatureOnlyReportOnce: Boolean = true

    internal var logLevel = Log.ERROR

    internal var expectMtuSize = 185

    internal var centralFeatureUseDeprecatedOnMessage = false

    internal val notificationDescriptorUuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @JvmField
    internal val notificationDescriptor = BluetoothGattDescriptor(
        notificationDescriptorUuid,
        BluetoothGattDescriptor.PERMISSION_READ
                or BluetoothGattDescriptor.PERMISSION_WRITE
    )

    internal lateinit var uuidForAdvertise: UUID
    internal lateinit var bleGattService: BleGattService

    @JvmStatic
    @JvmOverloads
    fun initialize(applicationContext: Context, options: BleKitOptions = BleKitOptions()) {
        this.applicationContext = Objects.requireNonNull(applicationContext)
        val builder = BleGattService.Builder(options.uuidForGattService)
        builder.addUuidForWritable(options.uuidForWritable)
        options.uuidsForNotification.forEach(builder::addUuidForNotification)
        bleGattService = builder.build()
        uuidForAdvertise = options.uuidForAdvertise
        advertiseSettings = AdvertiseSettings
            .Builder()
            .setConnectable(true)
            .setAdvertiseMode(options.leAdvertiseMode)
            .build()
        advertiseFeatureIncludeDeviceName = options.leAdvertiseFeatureIncludeDeviceName
        scanSettings = ScanSettings
            .Builder()
            .setScanMode(options.leScanMode)
            .build()
        scanFeatureOnlyReportOnce = options.leScanFeatureOnlyReportOnce
        expectMtuSize = options.expectMtuSize
        logLevel = options.logLevel
        centralFeatureUseDeprecatedOnMessage = options.centralFeatureUseDeprecatedOnMessage
    }

    @JvmStatic
    fun getAdvertiseUuid(): UUID = uuidForAdvertise
}