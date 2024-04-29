package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import com.outlook.wn123o.blekit.central.BleScanCallback

interface BleCentralApi: BleApi {
    fun connect(device: BluetoothDevice)
    fun disconnect(bleAddress: String)

    fun scanWithDuration(mills: Long, callback: BleScanCallback)
    fun scanWithDuration(mills: Long, callback: BleScanCallback, filters: List<ScanFilter>?)
    fun setCentralCallback(callback: BleCentralCallback)

    fun readRemoteRssi(bleAddress: String): Boolean
}