package com.outlook.wn123o.blekit.interfaces

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
     * @param bleAddress String
     * @param bytes ByteArray
     * @param offset Int
     */
    fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int)

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