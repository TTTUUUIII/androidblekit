package com.outlook.wn123o.blekit.common

import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.util.Log
import java.util.UUID

data class BleKitOptions @JvmOverloads constructor(
    var uuidForAdvertise: UUID = ble16BitUuid(0x14DB),
    var uuidForGattService: UUID = ble16BitUuid(0x14FB),
    var uuidForWritable: UUID = ble16BitUuid(0x14EB),
    var uuidsForNotification: List<UUID> = listOf(ble16BitUuid(0x14EC)),
    var leScanMode: Int = ScanSettings.SCAN_MODE_LOW_POWER,
    var leScanFeatureOnlyReportOnce: Boolean = true,
    var leAdvertiseMode: Int = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER,
    var leAdvertiseFeatureIncludeDeviceName: Boolean = false,
    var centralFeatureUseDeprecatedOnMessage: Boolean = false,
    var expectMtuSize: Int = MTU_DEFAULT_SIZE,
    var logLevel: Int = Log.ERROR
) {
    companion object {
        const val MTU_DEFAULT_SIZE = 0
    }
}
