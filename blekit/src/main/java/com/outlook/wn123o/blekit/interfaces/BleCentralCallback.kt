package com.outlook.wn123o.blekit.interfaces

interface BleCentralCallback: BleCallback {
    fun onReadyToWrite(bleAddress: String){}
    fun onReadRemoteRssi(bleAddress: String, rssi: Int){}
}