package com.outlook.wn123o.blekit.interfaces

internal interface BlePeripheralEventListener: BleCallback {
    /**
     * Some error happened
     * @param error Int
     */
    fun onError(error: Int) {}
}