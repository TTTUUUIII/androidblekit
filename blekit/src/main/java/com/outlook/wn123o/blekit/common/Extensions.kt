package com.outlook.wn123o.blekit.common

import android.os.SystemClock
import com.outlook.wn123o.blekit.Env.LOG_TAG
import android.util.Log
import java.util.concurrent.Executors

private val mExecutor = Executors.newSingleThreadExecutor()

internal fun <T> T.debug(msg: String) {
    Log.d(LOG_TAG, "[DEBUG] $msg")
}

internal fun <T> T.message(msg: String) {
    Log.i(LOG_TAG, "[INFO] $msg")
}

internal fun <T> T.runAtDelayed(mill: Long, action: Runnable) {
    mExecutor
        .submit {
            SystemClock.sleep(mill)
            action.run()
        }
}