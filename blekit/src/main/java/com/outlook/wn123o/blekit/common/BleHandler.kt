package com.outlook.wn123o.blekit.common

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import com.outlook.wn123o.blekit.interfaces.IHandler

class BleHandler: HandlerThread("BleHandler-${SystemClock.uptimeMillis()}"), IHandler {

    private lateinit var handler: Handler

    init {
        start()
    }

    override fun post(task: Runnable) {
        handler.post(task)
    }

    override fun postDelayed(task: Runnable, delayed: Long) {
        handler.postDelayed(task, delayed)
    }

    override fun postAtTime(task: Runnable, time: Long) {
        handler.postAtTime(task, time)
    }

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)
    }
}