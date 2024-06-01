package com.outlook.wn123o.androidblekit.ui

import android.view.View
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.getString
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import com.outlook.wn123o.blekit.peripheral.BlePeripheral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BlePeripheralFragmentViewModel: BaseViewModel(), BlePeripheralCallback {

    private val blePeripheral = BlePeripheral(this)

    private var _connectState = MutableStateFlow(getString(R.string.str_disconnected))
    val connectState = _connectState.asStateFlow()

    fun updateConnectState(newState: String) {
        _connectState.update {
            newState
        }
    }

    override fun onConnected(bleAddress: String) {
        updateConnectState(getString(R.string.str_connected))
        updateRemoteAddressState(bleAddress)
    }

    override fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int) {
        putMsg("${timeZone()}: ${String(bytes)}")
    }

    override fun onDisconnected(bleAddress: String) {
        updateConnectState(getString(R.string.str_disconnected))
        updateRemoteAddressState("")
        blePeripheral.startup()
    }

    private fun isConnected() = remoteAddressState.value.isNotEmpty()

    private fun writeBytes(bytes: ByteArray): Boolean {
        return if (isConnected()) {
            blePeripheral.writeBytes(remoteAddressState.value, bytes)
        } else {
            false
        }
    }

    override fun onAction(view: View) {
        when(view.id) {
            R.id.send_msg_button -> if (txMsg.isNotEmpty()) writeBytes(txMsg.encodeToByteArray())
        }
    }

    fun setBlePeripheralEnable(enable: Boolean) {
        if (enable) {
            blePeripheral.startup()
        } else {
            blePeripheral.shutdown()
        }
    }
}