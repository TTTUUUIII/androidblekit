package com.outlook.wn123o.blekit.common.scanner

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.outlook.wn123o.blekit.common.BleDevice

abstract class BleBaseScanAdapter: ScanCallback() {
    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let { scanResult ->
            onScanResult(
                BleDevice(
                    scanResult.device.address,
                    scanResult.scanRecord,
                    scanResult.device.name ?: "Unknown",
                    scanResult.rssi,
                    scanResult.device
                )
            )
        }
    }

    abstract fun onScanResult(bleDevice: BleDevice)

    open fun onScanStart() {}
    open fun onScanStop() {}
}