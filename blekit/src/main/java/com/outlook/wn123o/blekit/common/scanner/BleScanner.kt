package com.outlook.wn123o.blekit.common.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.Context
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.runAtDelayed

@SuppressLint("MissingPermission")
class BleScanner(private val adapter: BleBaseScanAdapter): ScanCallback() {

    private val mScanHistory = mutableSetOf<String>()
    private val mLeScanner by lazy {
        val bleManager = BleEnvironment.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleManager.adapter.bluetoothLeScanner
    }

    private var mScanning = false
    fun scanWithDuration(mills: Long) = scanWithDuration(mills, null)

    fun scanWithDuration(mills: Long, filters: List<ScanFilter>?) {
        if (!mScanning) {
            scan(filters ?: listOf())
            runAtDelayed(mills) {
                stopScan()
            }
        }
    }

    
    fun scan(filters: List<ScanFilter>) {
        if (!isScanning()) {
                mLeScanner.startScan(filters, BleEnvironment.scanSettings, this)
            mScanning = true
            adapter.onScanStart()
        }
    }

    
    fun stopScan() {
        if (isScanning()) {
                mLeScanner.stopScan(this)
            mScanning = false
            adapter.onScanStop()
            mScanHistory.clear()
        }
    }

    override fun onScanFailed(errorCode: Int) {
        adapter.onScanFailed(errorCode)
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let { scanResult ->
            val bleAddress = scanResult.device.address
            if (!mScanHistory.contains(bleAddress)) {
                adapter.onScanResult(callbackType, result)
            }
            if (BleEnvironment.scanFeatureOnlyReportOnce) {
                mScanHistory.add(bleAddress)
            }
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        adapter.onBatchScanResults(results)
    }

    fun isScanning(): Boolean = mScanning
}