package com.outlook.wn123o.blekit.interfaces

import android.bluetooth.BluetoothGatt
import androidx.annotation.IntDef

@IntDef(
    ConnectionState.CONNECTED,
    ConnectionState.CONNECTING,
    ConnectionState.DISCONNECTING,
    ConnectionState.DISCONNECTED
)
annotation class ConnectionState {
    companion object {
        const val CONNECTING = BluetoothGatt.STATE_CONNECTING
        const val CONNECTED = BluetoothGatt.STATE_CONNECTED
        const val DISCONNECTING = BluetoothGatt.STATE_DISCONNECTING
        const val DISCONNECTED = BluetoothGatt.STATE_DISCONNECTED

        @JvmStatic
        fun string(@ConnectionState state: Int): String = when(state) {
            CONNECTING -> "CONNECTING"
            CONNECTED -> "CONNECTED"
            DISCONNECTING -> "DISCONNECTING"
            else -> "DISCONNECTED"
        }
    }
}
