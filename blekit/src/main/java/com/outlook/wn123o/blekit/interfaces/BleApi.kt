package com.outlook.wn123o.blekit.interfaces

interface BleApi<in T> {
    fun writeBytes(bleAddress: String, bytes: ByteArray): Boolean

    fun registerCallback(callback: T)
    fun unregisterCallback()
}