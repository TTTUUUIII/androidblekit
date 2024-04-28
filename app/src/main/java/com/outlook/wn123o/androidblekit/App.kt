package com.outlook.wn123o.androidblekit

import android.app.Application
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import com.outlook.wn123o.blekit.BleKitScope
import com.outlook.wn123o.blekit.common.BleKitOptions

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val options = BleKitOptions(this)
        options.leScanMode = ScanSettings.SCAN_MODE_BALANCED
        options.leAdvertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED
        BleKitScope.initialize(options)
    }
}