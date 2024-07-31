package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

interface BleCallback {

    /**
     * Connected to target device.
     * @param bleAddress String
     */
    fun onConnected(bleAddress: String)

    /**
     * Data can be sent safely.
     * @param bleAddress String
     */
    fun onReadyToWrite(bleAddress: String){}

    /**
     * Received some data from target device.
     * @param characteristic UUID
     * @param address String
     * @param bytes ByteArray
     * @param offset Int
     */
    fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
        onMessage(address, bytes, offset)
    }

    /**
     * Deprecated
     * @see onMessage(characteristic: UUID, bleAddress: String, bytes: ByteArray, offset: Int)
     * @param address String
     * @param bytes ByteArray
     * @param offset Int
     */
    @Deprecated(
        "Deprecated use onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int)"
    )
    fun onMessage(address: String, bytes: ByteArray, offset: Int) {}

    /**
     * Already disconnected.
     * @param bleAddress String
     */
    fun onDisconnected(bleAddress: String)

    fun onMtuChanged(bleAddress: String, mtu: Int) {}

    /**
     * Some error happened
     * @param error Int
     */
    fun onError(error: Int) {}
}