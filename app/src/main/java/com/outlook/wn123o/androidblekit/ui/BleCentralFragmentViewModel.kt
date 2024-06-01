package com.outlook.wn123o.androidblekit.ui

import android.bluetooth.BluetoothDevice
import android.view.View
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.error
import com.outlook.wn123o.androidblekit.common.getString
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.blekit.central.BleCentral
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BleCentralFragmentViewModel: BaseViewModel(), BleCentralCallback {

    private val TAG = BleCentralFragmentViewModel::class.java.simpleName

    private val bleCentral = BleCentral(this)

    private var _connectState = MutableStateFlow("未连接")
    val connectState = _connectState.asStateFlow()

    private fun updateConnectState(newState: String) {
        _connectState.update {
            newState
        }
    }

    private var _remoteRssiState = MutableStateFlow(0)
    val remoteRssiState = _remoteRssiState.asStateFlow()

    fun updateRemoteRssiState(newState: Int) {
        _remoteRssiState.update {
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
    }

    override fun onReadRemoteRssi(bleAddress: String, rssi: Int) {
        updateRemoteRssiState(rssi)
    }

    private fun isConnected() = remoteAddressState.value.isNotEmpty()

    private fun writeBytes(bytes: ByteArray): Boolean {
        return if (isConnected()) {
            bleCentral.writeBytes(remoteAddressState.value, bytes)
        } else {
            false
        }
    }

    override fun onAction(view: View) {
        when(view.id) {
            R.id.send_msg_button -> if (txMsg.isNotEmpty()) writeBytes(txMsg.encodeToByteArray())
            R.id.refresh_rssi_button -> if (isConnected()) bleCentral.readRemoteRssi(remoteAddressState.value)
        }
    }

    fun connect(device: BluetoothDevice) {
        if (!isConnected()) {
            bleCentral.connect(device)
        } else {
            error(TAG, "Already connected to ${remoteAddressState.value}!")
        }
    }

    fun disconnect() {
        if (isConnected()) {
            bleCentral.disconnect(remoteAddressState.value)
        }
    }
}