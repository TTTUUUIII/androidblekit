package com.outlook.wn123o.blekit.common.advertiser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.common.warn

@SuppressLint("MissingPermission")
class BleAdvertiser(private val callback: AdvertiseCallback): AdvertiseCallback() {

    private val mLeAdvertiser by lazy {
        val bleManager = BleEnvironment.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleManager.adapter.bluetoothLeAdvertiser
    }

    private var mAdvertising = false
    fun startAdvertising(data: AdvertiseData) {
        if (isAdvertising()) {
            warn("Advertiser already advertising, please stop it first!")
            return
        }
        mLeAdvertiser.startAdvertising(BleEnvironment.advertiseSettings, data, this)
    }

    fun stopAdvertising() {
        if (isAdvertising()) {
            mLeAdvertiser.stopAdvertising(this)
            mAdvertising = false
        }
    }

    fun isAdvertising(): Boolean = mAdvertising

    override fun onStartFailure(errorCode: Int) {
        super.onStartFailure(errorCode)
        callback.onStartFailure(errorCode)
        error("Failed to start advertiser errorCode=$errorCode")
        mAdvertising = false
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        super.onStartSuccess(settingsInEffect)
        callback.onStartSuccess(settingsInEffect)
        mAdvertising = true
    }
}