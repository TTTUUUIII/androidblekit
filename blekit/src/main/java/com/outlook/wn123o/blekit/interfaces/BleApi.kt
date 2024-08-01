package com.outlook.wn123o.blekit.interfaces

interface BleApi<in T> {
    fun registerCallback(callback: T)
    fun unregisterCallback()
    fun isConnected(address: String? = null): Boolean
}