package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.le.AdvertiseData

interface BlePeripheralApi: BleApi {
    fun startup(advertiseData: AdvertiseData?)
    fun startup()
    fun shutdown()
    fun disconnect(bleAddress: String)
    fun setPeripheralCallback(callback: BlePeripheralCallback)
}