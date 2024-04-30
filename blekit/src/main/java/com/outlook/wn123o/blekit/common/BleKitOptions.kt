package com.outlook.wn123o.blekit.common

import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import android.util.Log

data class BleKitOptions @JvmOverloads constructor(
    var preferenceServiceUuid: String = ble16BitUuid("14FB"),
    var preferenceWritableChaUuid: String = ble16BitUuid("14EB"),
    var preferenceNotifyChaUuid: String = ble16BitUuid("14EC"),
    var leScanMode: Int = ScanSettings.SCAN_MODE_LOW_POWER,
    var leAdvertiseMode: Int = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER,
    var expectMtuSize: Int = 185,
    var logLevel: Int = Log.ERROR
)
