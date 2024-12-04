package com.outlook.wn123o.androidblekit.ui

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.Msg
import com.outlook.wn123o.androidblekit.common.getSetting
import com.outlook.wn123o.androidblekit.common.getString
import com.outlook.wn123o.androidblekit.common.newFileInDownloadsDir
import com.outlook.wn123o.androidblekit.common.requireApplicationContext
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import com.outlook.wn123o.blekit.peripheral.BlePeripheral
import com.outlook.wn123o.blekit.peripheral.BlePeripheralCallback
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

class BlePeripheralFragmentViewModel: BaseViewModel(), SimplePacketizer.Callback {

    private var _event = MutableLiveData<Int>()
    val event = _event
    private val blePeripheral = BlePeripheral(PeripheralCallback())
    private var simpleDataSlicer =
        SimplePacketizer(this, requireApplicationContext().getSetting(R.string.key_setting_default_mtu, "20").toInt())

    private var _connectState = MutableStateFlow(getString(R.string.str_disconnected))
    val connectState = _connectState.asStateFlow()

    private fun updateConnectState(newState: String) {
        _connectState.update {
            newState
        }
    }

    fun sendStream(inputStream: InputStream, extension: String) {
        simpleDataSlicer.encode(inputStream, extension, inputStream.available())
    }

    override fun onAction(view: View) {
        when(view.id) {
            R.id.send_msg_button -> if (txMsg.isNotEmpty()) simpleDataSlicer.encode(txMsg)
            R.id.select_file_button -> {
                _event.postValue(EVENT_SELECT_SEND_FILE)
            }
        }
    }

    fun setBlePeripheralEnable(enable: Boolean) {
        if (enable) {
            blePeripheral.startup()
        } else {
            blePeripheral.shutdown()
        }
    }

    private inner class PeripheralCallback: BlePeripheralCallback() {
        override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
            simpleDataSlicer.decode(bytes)
            Log.d("DEBUG", bytes.contentToString())
        }

        override fun onError(error: Int) {
            if (error == BlePeripheral.ERR_ADVERTISE_FAILED) {
                toast(R.string.str_advertise_failed)
            }
        }

        override fun onConnectionStateChanged(state: Int, address: String) {
            updateConnectState(ConnectionState.string(state))
            updateRemoteAddressState(if (state == ConnectionState.DISCONNECTED) "" else address)
            if (state == ConnectionState.DISCONNECTED) {
                blePeripheral.startup()
            }
        }

        override fun onMtuChanged(bleAddress: String, mtu: Int) {
            super.onMtuChanged(bleAddress, mtu)
            updateMtu(mtu)
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
    }

    override fun onTx(slice: ByteArray, sliceType: Int) {
        blePeripheral.writeBytes(slice)
    }
}