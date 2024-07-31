package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothGatt
import java.util.UUID

interface BleCentralCallback: BleCallback {
    fun onReadRemoteRssi(bleAddress: String, rssi: Int){}
    fun onServicesDiscovered(gatt: BluetoothGatt) {}
    /**
     * Some error happened
     * @param error Int
     */
    fun onError(error: Int, address: String?) {}
}