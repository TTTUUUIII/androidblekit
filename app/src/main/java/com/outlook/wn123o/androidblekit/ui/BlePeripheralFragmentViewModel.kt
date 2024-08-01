package com.outlook.wn123o.androidblekit.ui

import android.view.View
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.getString
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import com.outlook.wn123o.blekit.peripheral.BlePeripheral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BlePeripheralFragmentViewModel: BaseViewModel(), BlePeripheralCallback {

    private val blePeripheral = BlePeripheral(this)

    private var _connectState = MutableStateFlow(getString(R.string.str_disconnected))
    val connectState = _connectState.asStateFlow()

    private fun updateConnectState(newState: String) {
        _connectState.update {
            newState
        }
    }

    override fun onMessage(address: String, bytes: ByteArray, offset: Int) {
        putMsg("${timeZone()}: ${String(bytes)}")
    }

    override fun onError(error: Int) {
        if (error == BlePeripheral.ERR_ADVERTISE_FAILED) {
            toast(R.string.str_advertise_failed)
        }
    }

    override fun onConnectionStateChanged(state: Int, address: String) {
        updateConnectState(ConnectionState.string(state))
        updateRemoteAddressState(if (state == ConnectionState.DISCONNECTED) "" else address)
    }

    private fun writeBytes(bytes: ByteArray) {
        if (!blePeripheral.isConnected() || !blePeripheral.writeBytes(bytes)) {
            toast(R.string.str_send_failure)
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