package com.outlook.wn123o.blekit

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.outlook.wn123o.blekit.common.BleKitOptions
import java.util.UUID

object BleKitScope {

    @JvmStatic
    @JvmOverloads
    fun initialize(applicationContext: Context, options: BleKitOptions = BleKitOptions()) {
        BleEnv.applicationContext = applicationContext
        BleEnv.preferenceServiceUuid = UUID.fromString(options.preferenceServiceUuid)
        BleEnv.preferenceWriteChaUuid = UUID.fromString(options.preferenceWritableChaUuid)
        BleEnv.preferenceNotifyChaUuid = UUID.fromString(options.preferenceNotifyChaUuid)
        BleEnv.advertiseSettings = AdvertiseSettings
            .Builder()
            .setConnectable(true)
            .setAdvertiseMode(options.leAdvertiseMode)
            .build()
        BleEnv.scanSettings = ScanSettings
            .Builder()
            .setScanMode(options.leScanMode)
            .build()
        BleEnv.advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(
                ParcelUuid(BleEnv.preferenceServiceUuid)
            )
            .build()
        BleEnv.scanFeatureOnlyReportOnce = options.leScanFeatureOnlyReportOnce
        BleEnv.expectMtuSize = options.expectMtuSize
        BleEnv.logLevel = options.logLevel
    }

    @JvmStatic
    fun getServiceUuid() = BleEnv.preferenceServiceUuid
}