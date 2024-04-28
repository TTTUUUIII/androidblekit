package com.outlook.wn123o.androidblekit

import androidx.lifecycle.ViewModel
import com.outlook.wn123o.blekit.central.BleCentral
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import com.outlook.wn123o.blekit.peripheral.BlePeripheral

class MainActivityViewModel: ViewModel() {
    private val mBleCentral by lazy {
        BleCentral()
    }

    fun getBleCentral(callback: BleCentralCallback? = null): BleCentral {
        if (callback != null) {
            mBleCentral.setCentralCallback(callback)
        }
        return mBleCentral
    }

    private val mBlePeripheral by lazy {
        BlePeripheral()
    }

    fun getBlePeripheral(callback: BlePeripheralCallback): BlePeripheral {
        mBlePeripheral.setPeripheralCallback(callback)
        return mBlePeripheral
    }
}