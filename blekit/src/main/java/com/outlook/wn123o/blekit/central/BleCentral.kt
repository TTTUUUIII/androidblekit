package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.outlook.wn123o.blekit.BleEnv
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.mainScope
import com.outlook.wn123o.blekit.common.runAtDelayed
import com.outlook.wn123o.blekit.interfaces.BleCentralApi
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import kotlinx.coroutines.launch

class BleCentral(private var mExternCallback: BleCentralCallback? = null): BleCentralCallback, BleCentralApi {
    private val mCtx = BleEnv.applicationContext

    private val mConnections = mutableMapOf<String, BleGattCallbackImpl>()

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice) {
        debug("connect to ${device.address}")
        mConnections[device.address] = BleGattCallbackImpl(mCtx, device, this)
    }

    override fun onConnected(bleAddress: String) {
        mainScope()
            .launch {
                mExternCallback?.onConnected(bleAddress)
            }
    }

    override fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int) {
        mainScope()
            .launch {
                mExternCallback?.onMessage(bleAddress, bytes, offset)
            }
    }

    override fun onDisconnected(bleAddress: String) {
        mConnections.remove(bleAddress)
        mainScope()
            .launch {
                mExternCallback?.onDisconnected(bleAddress)
            }
    }

    override fun onMtuChanged(bleAddress: String, mtu: Int) {
        mainScope()
            .launch {
            mExternCallback?.onMtuChanged(bleAddress, mtu)
        }
    }

    override fun onReadyToWrite(bleAddress: String) {
        runAtDelayed(200L) {
            mainScope()
                .launch {
                    mExternCallback?.onReadyToWrite(bleAddress)
                }
        }
    }

    override fun onReadRemoteRssi(bleAddress: String, rssi: Int) {
        mainScope()
            .launch {
            mExternCallback?.onReadRemoteRssi(bleAddress, rssi)
        }
    }

    override fun onError(error: Int) {
        mainScope()
            .launch {
            mExternCallback?.onError(error)
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect(bleAddress: String) {
        mConnections
            .remove(bleAddress)
            ?.close()
    }

    override fun writeBytes(bleAddress: String, bytes: ByteArray): Boolean {
        mConnections[bleAddress]
            ?.also {
                it.writeBytes(bytes)
                return true
            }
        return false
    }

    override fun registerCallback(callback: BleCentralCallback) {
        mExternCallback = callback
    }

    override fun unregisterCallback() {
        mExternCallback = null
    }

    override fun readRemoteRssi(bleAddress: String): Boolean {
        mConnections[bleAddress]?.let {
            it.readRemoteRssi()
            return true
        }
        return false
    }

    companion object {
        const val ERR_WRITE_CHARACTERISTIC_NOT_FOUND = -1
        const val ERR_NOTIFY_CHARACTERISTIC_NOT_FOUND = -2
    }
}