package com.outlook.wn123o.blekit.interfaces

interface BleCallback {
    fun onConnected(bleAddress: String)

    fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int)

    fun onDisconnected(bleAddress: String)

    fun onMtuChanged(bleAddress: String, mtu: Int) {}

    fun onError(error: Int) {}
}