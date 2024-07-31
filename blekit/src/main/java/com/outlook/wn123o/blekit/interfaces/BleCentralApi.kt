package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothDevice

interface BleCentralApi: BleApi<BleCentralCallback> {
    fun writeBytes(address: String, bytes: ByteArray): Boolean
    fun connect(device: BluetoothDevice)
    fun disconnect(bleAddress: String)
    fun readRemoteRssi(bleAddress: String): Boolean
}