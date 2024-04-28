package com.outlook.wn123o.blekit.common

import android.app.Application
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings

data class BleKitOptions @JvmOverloads constructor(
    var serviceUuid: String = ble16BitUuid("14FB"),
    var writableChaUuid: String = ble16BitUuid("14EB"),
    var notifyChaUuid: String = ble16BitUuid("14EC"),
    var leScanMode: Int = ScanSettings.SCAN_MODE_LOW_POWER,
    var leAdvertiseMode: Int = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER,
    var expectMtuSize: Int = 185,
    var debug: Boolean = false
)
