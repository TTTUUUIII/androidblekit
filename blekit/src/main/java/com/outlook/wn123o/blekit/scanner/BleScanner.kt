package com.outlook.wn123o.blekit.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.Context
import com.outlook.wn123o.blekit.BleEnv
import com.outlook.wn123o.blekit.common.runAtDelayed

class BleScanner: ScanCallback() {

    private val mScanHistory = mutableSetOf<String>()
    private val mLeScanner by lazy {
        val bleManager = BleEnv.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleManager.adapter.bluetoothLeScanner
    }

    private var mExternScanAdapter: BleBaseScanAdapter? = null

    private var mScanning = false
    fun scanWithDuration(mills: Long, callback: BleBaseScanAdapter) = scanWithDuration(mills, callback, null)

    fun scanWithDuration(mills: Long, scanAdapter: BleBaseScanAdapter, filters: List<ScanFilter>?) {
        if (!mScanning) {
            scan(scanAdapter, filters ?: listOf())
            runAtDelayed(mills) {
                stopScan()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun scan(scanAdapter: BleBaseScanAdapter, filters: List<ScanFilter>) {
        mExternScanAdapter = scanAdapter
        if (!mScanning) {
                mLeScanner.startScan(filters, BleEnv.scanSettings, this)
            mScanning = true
            scanAdapter.onScanStart()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (mScanning) {
                mLeScanner.stopScan(this)
            mScanning = false
            mExternScanAdapter?.onScanStop()
            mExternScanAdapter = null
            mScanHistory.clear()
        }
    }

    override fun onScanFailed(errorCode: Int) {
        mExternScanAdapter?.onScanFailed(errorCode)
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let { scanResult ->
            val bleAddress = scanResult.device.address
            if (!mScanHistory.contains(bleAddress)) {
                mExternScanAdapter?.onScanResult(callbackType, result)
            }
            if (BleEnv.scanFeatureOnlyReportOnce) {
                mScanHistory.add(bleAddress)
            }
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        mExternScanAdapter?.onBatchScanResults(results)
    }

    fun isScanning(): Boolean = mScanning
}