package com.outlook.wn123o.androidblekit.ui

import android.bluetooth.BluetoothDevice
import android.view.View
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.error
import com.outlook.wn123o.androidblekit.common.getString
import com.outlook.wn123o.androidblekit.common.requireApplicationContext
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.blekit.central.BleCentral
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

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

    override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
        putMsg("${timeZone()}: ${String(bytes)}")
    }

    override fun onReadRemoteRssi(bleAddress: String, rssi: Int) {
        updateRemoteRssiState(rssi)
    }

    override fun onError(error: Int, address: String?) {
        super.onError(error, address)
        if (error == BleCentral.ERR_CONNECT_FAILED) {
            toast(R.string.str_connection_failed)
        }
    }

    override fun onConnectionStateChanged(state: Int, address: String) {
        updateConnectState(ConnectionState.string(state))
        updateRemoteAddressState(if (state == ConnectionState.DISCONNECTED) "" else address)
    }

    private fun writeBytes(bytes: ByteArray) {
        if (!bleCentral.isConnected() || !bleCentral.writeBytes(remoteAddressState.value, bytes)) {
            toast(R.string.str_send_failure)
        }
    }

    override fun onAction(view: View) {
        when(view.id) {
            R.id.send_msg_button -> if (txMsg.isNotEmpty()) writeBytes(txMsg.encodeToByteArray())
            R.id.refresh_rssi_button -> if (bleCentral.isConnected(null)) bleCentral.readRemoteRssi(remoteAddressState.value)
        }
    }

    fun connect(device: BluetoothDevice) = bleCentral.connect(device)

    fun disconnect() {
        if (bleCentral.isConnected()) {
            bleCentral.disconnect(remoteAddressState.value)
        }
    }
}