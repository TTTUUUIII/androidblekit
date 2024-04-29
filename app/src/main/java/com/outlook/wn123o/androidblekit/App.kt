package com.outlook.wn123o.androidblekit

import android.app.Application
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.util.Log
import com.outlook.wn123o.blekit.BleKitScope
import com.outlook.wn123o.blekit.common.BleKitOptions

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val options = BleKitOptions()
        options.leScanMode = ScanSettings.SCAN_MODE_BALANCED
        options.leAdvertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED
        options.logLevel = Log.DEBUG
        BleKitScope.initialize(this, options)
    }
}