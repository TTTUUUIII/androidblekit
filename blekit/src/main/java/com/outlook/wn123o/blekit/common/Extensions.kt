package com.outlook.wn123o.blekit.common

import android.bluetooth.BluetoothGattCharacteristic
import android.os.SystemClock
import com.outlook.wn123o.blekit.BleEnvironment.LOG_TAG
import android.util.Log
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.util.BleKitUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

internal fun <T> T.message(msg: String) {
    if (BleEnvironment.logLevel <= Log.INFO) {
        Log.i(LOG_TAG, "[INFO] $msg")
    }
}

internal fun <T> T.runAtDelayed(mill: Long, action: Runnable) {
    globalScope.launch(Dispatchers.IO) {
        SystemClock.sleep(mill)
        withContext(Dispatchers.Main) {
            action.run()
        }
    }
}

internal fun <T> T.runOnUiThread(action: Runnable) {
    action.run()
}

private val globalScope = CoroutineScope(Dispatchers.Main)

internal fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = properties and property == property

internal fun ble16BitUuid(v: Long): UUID = BleKitUtils.ble16BitUuid(v)