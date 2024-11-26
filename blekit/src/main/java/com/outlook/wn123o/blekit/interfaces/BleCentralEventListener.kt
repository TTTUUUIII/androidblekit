package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothGatt
import java.util.UUID

internal interface BleCentralEventListener: BleCallback {
    fun onReadRemoteRssi(bleAddress: String, rssi: Int){}
    fun onServicesDiscovered(gatt: BluetoothGatt) {}
    fun onCharacteristicWrite(uuid: UUID, success: Boolean) {}
    /**
     * Some error happened
     * @param error Int
     */
    fun onError(error: Int, address: String?) {}
    
    fun onCharacteristicRegistered(characteristic: UUID, notify: Boolean) {}
}