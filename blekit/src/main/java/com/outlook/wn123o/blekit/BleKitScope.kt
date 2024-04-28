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
        Env.serviceUuid = UUID.fromString(options.serviceUuid)
        Env.writeChaUuid = UUID.fromString(options.writableChaUuid)
        Env.notifyChaUuid = UUID.fromString(options.notifyChaUuid)
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
                ParcelUuid(Env.serviceUuid)
            )
            .build()
        Env.expectMtuSize = options.expectMtuSize
        Env.debug = options.debug
    }

    @JvmStatic
    fun getServiceUuid() = Env.serviceUuid
}