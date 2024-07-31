package com.outlook.wn123o.blekit.interfaces

interface BlePeripheralCallback: BleCallback {
    /**
     * Some error happened
     * @param error Int
     */
    fun onError(error: Int) {}
}