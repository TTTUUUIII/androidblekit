package com.outlook.wn123o.blekit.common

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.SystemClock
import com.outlook.wn123o.blekit.Env.LOG_TAG
import android.util.Log
import com.outlook.wn123o.blekit.Env
import java.util.concurrent.Executors

private val mExecutor = Executors.newSingleThreadExecutor()

internal fun <T> T.debug(msg: String) {
    if (Env.logLevel <= Log.DEBUG) {
        Log.d(LOG_TAG, "[DEBUG] $msg")
    }
}

internal fun <T> T.error(msg: String) {
    if (Env.logLevel <= Log.ERROR) {
        Log.e(LOG_TAG, "[ERROR] $msg")
    }
}

internal fun <T> T.message(msg: String) {
    if (Env.logLevel <= Log.INFO) {
        Log.i(LOG_TAG, "[INFO] $msg")
    }
}

internal fun <T> T.runAtDelayed(mill: Long, action: Runnable) {
    mExecutor
        .submit {
            SystemClock.sleep(mill)
            action.run()
        }
}

private fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = properties and property == property
internal fun BluetoothGattCharacteristic.hasProperties(vararg properties: Int): Boolean {
    properties.forEach { property ->
        if (!hasProperty(property)) {
            return false
        }
    }
    return true
}

internal fun BluetoothGattService.findCharacteristic(property: Int): BluetoothGattCharacteristic? = characteristics.find {
    it.hasProperty(property)
}

internal fun ble16BitUuid(ost: String) = "0000$ost-0000-1000-8000-00805F9B34FB"