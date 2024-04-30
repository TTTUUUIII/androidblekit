package com.outlook.wn123o.blekit

import android.app.Application
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.outlook.wn123o.blekit.common.BleKitOptions
import java.util.UUID

object BleKitScope {

    @JvmStatic
    @JvmOverloads
    fun initialize(ctx: Application, options: BleKitOptions = BleKitOptions()) {
        Env.context = ctx
        Env.preferenceServiceUuid = UUID.fromString(options.preferenceServiceUuid)
        Env.preferenceWriteChaUuid = UUID.fromString(options.preferenceWritableChaUuid)
        Env.preferenceNotifyChaUuid = UUID.fromString(options.preferenceNotifyChaUuid)
        Env.advertiseSettings = AdvertiseSettings
            .Builder()
            .setConnectable(true)
            .setAdvertiseMode(options.leAdvertiseMode)
            .build()
        Env.scanSettings = ScanSettings
            .Builder()
            .setScanMode(options.leScanMode)
            .build()
        Env.advertiseData = AdvertiseData.Builder()
            .addServiceUuid(
                ParcelUuid(Env.preferenceServiceUuid)
            )
            .build()
        Env.expectMtuSize = options.expectMtuSize
        Env.logLevel = options.logLevel
    }

    @JvmStatic
    fun getServiceUuid() = Env.preferenceServiceUuid
}