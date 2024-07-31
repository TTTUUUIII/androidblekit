package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.le.AdvertiseData
import java.util.UUID

interface BlePeripheralApi: BleApi<BlePeripheralCallback> {
    fun startup(advertiseData: AdvertiseData)
    fun startup(
        manufacturerId: Int? = null,
        manufacturerData: ByteArray? = null,
        dataServiceUUID: UUID? = null,
        dataServiceData: ByteArray? = null)
    fun shutdown()
    fun disconnect()
    fun writeBytes(bytes: ByteArray): Boolean
    fun writeBytes(characteristic: UUID, bytes: ByteArray): Boolean
}