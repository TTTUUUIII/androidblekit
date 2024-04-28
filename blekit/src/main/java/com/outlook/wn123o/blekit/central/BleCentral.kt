package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.content.Context
import com.outlook.wn123o.blekit.Env
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.runAtDelayed
import com.outlook.wn123o.blekit.interfaces.BleCentralApi
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback

class BleCentral(private var mExternCallback: BleCentralCallback? = null): BleCentralCallback, BleCentralApi {
    private val mCtx = Env.context
    private val mBleManager by lazy {
        mCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private var mScanning = false

    private val mConnections = mutableMapOf<String, BleGattCallbackImpl>()

    override fun scanWithDuration(mills: Long, callback: BleScanCallback) = scanWithDuration(mills, callback, null)

    override fun scanWithDuration(mills: Long, callback: BleScanCallback, filters: List<ScanFilter>?) {
        if (!mScanning) {
            scan(callback, filters ?: listOf())
            runAtDelayed(mills) {
                stopScan(callback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun scan(callback: BleScanCallback, filters: List<ScanFilter>) {
        if (!mScanning) {
            mBleManager
                .adapter
                .bluetoothLeScanner
                .startScan(filters, Env.scanSettings, callback)
            mScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan(callback: ScanCallback) {
        if (mScanning) {
            mBleManager
                .adapter
                .bluetoothLeScanner
                .stopScan(callback)
            mScanning = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice) {
        debug("connect to ${device.address}")
        mConnections[device.address] = BleGattCallbackImpl(mCtx, device, this)
    }

    override fun onConnected(bleAddress: String) {
        mExternCallback?.onConnected(bleAddress)
    }

    override fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int) {
        mExternCallback?.onMessage(bleAddress, bytes, offset)
    }

    override fun onDisconnected(bleAddress: String) {
        mConnections.remove(bleAddress)
        mExternCallback?.onDisconnected(bleAddress)
    }

    override fun onMtuChanged(bleAddress: String, mtu: Int) {
        mExternCallback?.onMtuChanged(bleAddress, mtu)
    }

    @SuppressLint("MissingPermission")
    override fun disconnect(bleAddress: String) {
        mConnections
            .remove(bleAddress)
            ?.close()
    }

    override fun send(bleAddress: String, bytes: ByteArray): Boolean {
        mConnections[bleAddress]
            ?.also {
                it.send(bytes)
                return true
            }
        return false
    }

    override fun setCentralCallback(callback: BleCentralCallback) {
        mExternCallback = callback
    }
}