package com.outlook.wn123o.blekit.common

import android.app.Application
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings

data class BleKitOptions(
    val app: Application,
    var serviceUuid: String = "39cde689-fcfc-45d5-841f-bb180e7ca559",
    var writableChaUuid: String = "a1803036-736d-47bb-b5ea-3579d2c60dc5",
    var notifyChaUuid: String = "31db4fb2-c94a-4d2b-bbb5-02baa1386eae",
    var leScanMode: Int = ScanSettings.SCAN_MODE_BALANCED,
    var leAdvertiseMode: Int = AdvertiseSettings.ADVERTISE_MODE_BALANCED,
    var expectMtuSize: Int = 185,
    var debug: Boolean = false
)
