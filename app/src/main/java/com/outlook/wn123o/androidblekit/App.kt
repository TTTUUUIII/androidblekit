package com.outlook.wn123o.androidblekit

import android.app.Application
import com.outlook.wn123o.blekit.BleKitScope
import com.outlook.wn123o.blekit.common.BleKitOptions

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val options = BleKitOptions(this)
        BleKitScope.initialize(options)
    }
}