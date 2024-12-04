package com.outlook.wn123o.androidblekit.ui

import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.Msg
import com.outlook.wn123o.androidblekit.common.getSetting
import com.outlook.wn123o.androidblekit.common.newFileInDownloadsDir
import com.outlook.wn123o.androidblekit.common.requireApplicationContext
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.blekit.central.BleCentral
import com.outlook.wn123o.blekit.central.BleCentralCallback
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import com.outlook.wn123o.blekit.util.SimplePacketizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BleCentralFragmentViewModel: BaseViewModel(), SimplePacketizer.Callback {

    private val TAG = BleCentralFragmentViewModel::class.java.simpleName
    private var _event = MutableLiveData<Int>()
    val event = _event

    private var simpleDataSlicer =
        SimplePacketizer(this, requireApplicationContext().getSetting(R.string.key_setting_default_mtu, "20").toInt())

    private val bleCentral = BleCentral(CentralCallback())

    var mtuInText = requireApplicationContext().getSetting(R.string.key_setting_default_mtu, "20")

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

    fun sendStream(inputStream: InputStream, extension: String) {
        simpleDataSlicer.encode(inputStream, extension, inputStream.available())
    }

    override fun onAction(view: View) {
        when(view.id) {
            R.id.send_msg_button -> if (txMsg.isNotEmpty()) simpleDataSlicer.encode(txMsg)
            R.id.refresh_rssi_button -> if (bleCentral.isConnected(null)) bleCentral.readRemoteRssi(remoteAddressState.value)
            R.id.request_mtu_button -> {
                try {
                    val newMtu = mtuInText.toInt()
                    if (remoteAddressState.value.isNotEmpty()) bleCentral.requestMtu(remoteAddressState.value, newMtu)
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireApplicationContext(), R.string.failure, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.select_file_button -> {
                _event.postValue(EVENT_SELECT_SEND_FILE)
            }
        }
    }

    fun connect(device: BluetoothDevice) = bleCentral.connect(device)

    fun disconnect() = bleCentral.disconnect(remoteAddressState.value)

    private inner class CentralCallback: BleCentralCallback() {
        override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
            simpleDataSlicer.decode(bytes)
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

        override fun onMtuChanged(bleAddress: String, mtu: Int) {
            super.onMtuChanged(bleAddress, mtu)
            updateMtu(mtu)
            mtuInText = mtu.toString()
        }
    }

    companion object {
        const val EVENT_SELECT_SEND_FILE = 1
    }

    private var outStream: OutputStream? = null
    private var outFile: File? = null

    override fun onRxBegin(extension: String, length: Int) {
        setProgressMax(length)
        setProgress(0)
        when(extension) {
            "str" -> {
                outStream = ByteArrayOutputStream()
            }
            else -> {
                outFile = newFileInDownloadsDir(extension)
                outStream = FileOutputStream(outFile)
            }
        }
    }

    override fun onRx(slice: ByteArray) {
        outStream?.write(slice)
        setProgress(progress.value + slice.size)
    }

    override fun onRxEnd() {
        outStream?.flush()
        outStream?.close()
        if (outStream is ByteArrayOutputStream) {
            putMsg(Msg.TYPE_TEXT, outStream.toString())
        } else {
            putMsg(Msg.TYPE_FILE, outFile!!.absolutePath)
        }
        outStream = null
        outFile = null
    }

    override fun onTx(slice: ByteArray, sliceType: Int) {
        bleCentral.writeBytes(remoteAddressState.value, slice)
    }
}