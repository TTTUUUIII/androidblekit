package com.outlook.wn123o.blekit.interfaces

interface BleApi {
    fun send(bleAddress: String, bytes: ByteArray): Boolean
}