package com.outlook.wn123o.androidblekit.ui

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.blekit.BleKitScope
import com.outlook.wn123o.blekit.common.BleDevice
import com.outlook.wn123o.blekit.scanner.BleBaseScanAdapter
import com.outlook.wn123o.blekit.scanner.BleScanner

class BleScanFragmentViewModel: ViewModel() {

    val scanResultLiveData = MutableLiveData<BleDevice?>(null)

    private val bleScanner = BleScanner()
    private val bleScanAdapter = BleScanCallbackAdapter()
    fun stopDiscover() = bleScanner.stopScan()

    val deviceNamePatternLiveData = MutableLiveData<String>(".*.*")

    private fun startDiscover() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleKitScope.getServiceUuid()))
            .build()
        bleScanner.scanWithDuration(1000 * 5, bleScanAdapter, listOf(filter))
    }

    fun onAction(view: View) {
        when(view.id) {
            R.id.btn_scan -> startDiscover()
            else -> {}
        }
    }

    inner class BleScanCallbackAdapter: BleBaseScanAdapter() {
        override fun onScanResult(bleDevice: BleDevice) {
            scanResultLiveData.postValue(bleDevice)
        }
    }
}