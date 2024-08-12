package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.le.AdvertiseData
import com.outlook.wn123o.blekit.peripheral.BlePeripheralCallback
import java.util.UUID

interface BlePeripheralApi: BleApi<BlePeripheralCallback> {
    fun startup()
    fun startup(advertiseData: AdvertiseData)
    fun startup(
        manufacturerId: Int?,
        manufacturerData: ByteArray?,
        dataServiceUUID: UUID?,
        dataServiceData: ByteArray?)
    fun shutdown()
    fun disconnect()
    fun writeBytes(bytes: ByteArray): Boolean
    fun writeBytes(characteristic: UUID, bytes: ByteArray): Boolean
}