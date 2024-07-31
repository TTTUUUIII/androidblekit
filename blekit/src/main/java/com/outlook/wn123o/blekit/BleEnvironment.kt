package com.outlook.wn123o.blekit

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.outlook.wn123o.blekit.common.BleKitOptions
import java.util.Objects
import java.util.UUID

object BleEnvironment {

    internal const val LOG_TAG = "AndroidBleKit"

    internal lateinit var applicationContext: Context

    internal lateinit var advertiseSettings: AdvertiseSettings
    internal lateinit var scanSettings: ScanSettings
    internal var scanFeatureOnlyReportOnce: Boolean = true

    internal var logLevel = Log.ERROR

    internal var expectMtuSize = 185

    internal val notificationDescriptorUuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    internal val notificationDescriptor = BluetoothGattDescriptor(
        notificationDescriptorUuid,
        BluetoothGattDescriptor.PERMISSION_READ
                or BluetoothGattDescriptor.PERMISSION_WRITE
    )

    internal lateinit var uuidForAdvertise: UUID
    internal lateinit var uuidForGattService: UUID
    internal val uuidsForNotification = mutableListOf<UUID>()
    internal lateinit var uuidForWrite: UUID

    @JvmStatic
    @JvmOverloads
    fun initialize(applicationContext: Context, options: BleKitOptions = BleKitOptions()) {
        this.applicationContext = Objects.requireNonNull(applicationContext)
        options.uuidsForNotification.forEach(uuidsForNotification::add)
        uuidForWrite = options.uuidForWrite
        uuidForGattService = options.uuidForGattService
        uuidForAdvertise = options.uuidForAdvertise
        advertiseSettings = AdvertiseSettings
            .Builder()
            .setConnectable(true)
            .setAdvertiseMode(options.leAdvertiseMode)
            .build()
        scanSettings = ScanSettings
            .Builder()
            .setScanMode(options.leScanMode)
            .build()
        scanFeatureOnlyReportOnce = options.leScanFeatureOnlyReportOnce
        expectMtuSize = options.expectMtuSize
        logLevel = options.logLevel
    }

    @JvmStatic
    fun getAdvertiseUuid(): UUID = uuidForAdvertise

    internal fun buildCharacteristicsForNotification(): List<BluetoothGattCharacteristic> {
        val characteristics = mutableListOf<BluetoothGattCharacteristic>()
        uuidsForNotification.forEach { uuid ->
            characteristics.add(BluetoothGattCharacteristic(
                uuid,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PERMISSION_READ,
                BluetoothGattCharacteristic.PERMISSION_READ))
        }
        return characteristics
    }
}