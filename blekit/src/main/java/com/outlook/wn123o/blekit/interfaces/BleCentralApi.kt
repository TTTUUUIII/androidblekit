package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothDevice

interface BleCentralApi: BleApi<BleCentralCallback> {
    fun connect(device: BluetoothDevice)
    fun disconnect(bleAddress: String)
    fun readRemoteRssi(bleAddress: String): Boolean
}