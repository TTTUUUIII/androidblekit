package com.outlook.wn123o.blekit.interfaces

interface BleCentralCallback: BleCallback {
    fun onReadRemoteRssi(bleAddress: String, rssi: Int){}
}