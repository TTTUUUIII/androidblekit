package com.outlook.wn123o.blekit.common

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Handler
import android.os.Looper
import com.outlook.wn123o.blekit.BleEnvironment.LOG_TAG
import android.util.Log
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.util.BleKitUtils
import java.util.UUID

internal fun <T> T.debug(msg: String) {
    if (BleEnvironment.logLevel <= Log.DEBUG) {
        Log.d(LOG_TAG, "[DEBUG] $msg")
    }
}

internal fun <T> T.error(msg: String) {
    if (BleEnvironment.logLevel <= Log.ERROR) {
        Log.e(LOG_TAG, "[ERROR] $msg")
    }
}

internal fun <T> T.warn(msg: String) {
    if (BleEnvironment.logLevel <= Log.WARN) {
        Log.w(LOG_TAG, "[WARN] $msg")
    }
}

internal fun <T> T.info(msg: String) {
    message(msg)
}

internal fun <T> T.message(msg: String) {
    if (BleEnvironment.logLevel <= Log.INFO) {
        Log.i(LOG_TAG, "[INFO] $msg")
    }
}

internal fun <T> T.runAtDelayed(mill: Long, action: Runnable) {
    handler.postDelayed(action, mill)
}

internal fun <T> T.runOnUiThread(action: Runnable) {
    handler.post(action)
}

private val handler = Handler(Looper.getMainLooper())

internal fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = properties and property == property

internal fun ble16BitUuid(v: Int): UUID = BleKitUtils.ble16BitUuid(v)