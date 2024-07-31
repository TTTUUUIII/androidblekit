package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothDevice
import java.util.UUID

interface BleCentralApi: BleApi<BleCentralCallback> {
    fun writeBytes(address: String, data: ByteArray): Boolean
    fun writeBytes(address: String, data: ByteArray, characteristic: UUID?): Boolean
    fun connect(device: BluetoothDevice)
    fun disconnect(bleAddress: String)
    fun readRemoteRssi(bleAddress: String): Boolean
}