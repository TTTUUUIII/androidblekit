package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.le.AdvertiseData

interface BlePeripheralApi: BleApi<BlePeripheralCallback> {
    fun startup(advertiseData: AdvertiseData)
    fun startup(manufacturerId: Int, manufacturerData: ByteArray)
    fun startup()
    fun shutdown()
    fun disconnect(bleAddress: String)
}