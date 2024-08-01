package com.outlook.wn123o.blekit.interfaces

import java.util.UUID

interface BleCallback {

    /**
     * Connect state changed.
     * @param state Int
     */
    fun onConnectStateChanged(@ConnectionState state: Int, address: String)

    @Deprecated(
        "Deprecated use onConnectStateChanged(state: Int, address: String)"
    )
    /**
     * Deprecated
     * @see onConnectStateChanged
     * Connected to target device.
     * @param bleAddress String
     */
    fun onConnected(bleAddress: String, ) {}

    /**
     * Deprecated
     * @see onConnectStateChanged
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

    @Deprecated(
        "Deprecated use onConnectStateChanged(state: Int, address: String)"
    )
    /**
     * @see onConnectStateChanged
     * Already disconnected.
     * @param bleAddress String
     */
    fun onDisconnected(bleAddress: String) {}

    fun onMtuChanged(bleAddress: String, mtu: Int) {}
}