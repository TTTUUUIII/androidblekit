package com.outlook.wn123o.androidblekit

import android.app.Application
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.outlook.wn123o.androidblekit.common.getSetting
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.BleKitOptions
import java.lang.ref.WeakReference

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val options = BleKitOptions()
        options.leScanMode = ScanSettings.SCAN_MODE_BALANCED
        options.leAdvertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED
        options.logLevel = parseLogLevel(getSetting(R.string.key_setting_log_level, "DEBUG"))
        options.leAdvertiseFeatureIncludeDeviceName = getSetting(R.string.key_setting_advertise_device_name, true)
        options.expectMtuSize = getSetting(R.string.key_setting_default_mtu, "20").toInt()
        BleEnvironment.initialize(this, options)
        sGlobalContextRef = WeakReference(this)
    }

    companion object {
        private lateinit var sGlobalContextRef: WeakReference<Context>

        fun getGlobalContext() = sGlobalContextRef.get()
    }

    private fun parseLogLevel(level: String): Int = when(level) {
        "DEBUG" -> Log.DEBUG
        "WARN" -> Log.WARN
        "ERROR" -> Log.ERROR
        else -> Log.INFO
    }
}