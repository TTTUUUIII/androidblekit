package com.outlook.wn123o.blekit.interfaces

interface BleApi {
    fun writeBytes(bleAddress: String, bytes: ByteArray): Boolean
}