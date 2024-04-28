package com.outlook.wn123o.blekit.common

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

data class BleDevice(
    val bleAddress: String,
    var scanRecord: ScanRecord? = null,
    var deviceName: String? = "Unknown",
    var rssi: Int = -999,
    val device: BluetoothDevice
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleDevice

        if (bleAddress != other.bleAddress) return false
        if (deviceName != other.deviceName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bleAddress.hashCode()
        result = 31 * result + (deviceName?.hashCode() ?: 0)
        return result
    }
}