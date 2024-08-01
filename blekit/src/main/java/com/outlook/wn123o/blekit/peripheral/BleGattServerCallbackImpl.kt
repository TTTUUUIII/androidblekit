package com.outlook.wn123o.blekit.peripheral

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.message
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import java.util.UUID

@SuppressLint("MissingPermission")
internal class BleGattServerCallbackImpl() : BluetoothGattServerCallback() {

    private var mGattServer: BluetoothGattServer? = null
    lateinit var callback: BlePeripheralCallback

    private val mCharacteristicsForNotification by lazy { BleEnvironment.bleGattService.characteristicsForNotification }

    private var mConnection: BluetoothDevice? = null
    private var mState = ConnectionState.DISCONNECTED

    init { prepareGatt() }

    private fun prepareGatt() {
        val context = BleEnvironment.applicationContext
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mGattServer = bluetoothManager.openGattServer(context, this)
        mGattServer!!.clearServices()
        mGattServer!!.addService(BleEnvironment.bleGattService.service)
    }

    fun releaseGatt() {
        mGattServer?.clearServices()
        mGattServer?.close()
        mGattServer = null
    }
    
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        debug("onConnectionStateChange: {state=$status, newState=$newState}")
        mState = newState
        when (newState) {
            BluetoothGatt.STATE_CONNECTING -> {
                callback.onConnectStateChanged(ConnectionState.CONNECTING, device.address)
            }
            BluetoothGatt.STATE_CONNECTED -> {
                mGattServer!!.connect(device, false)
                callback.onConnectStateChanged(ConnectionState.CONNECTED, device.address)
                mConnection = device
            }
            BluetoothGatt.STATE_DISCONNECTING -> {
                callback.onConnectStateChanged(ConnectionState.DISCONNECTING, device.address)
            }
            BluetoothGatt.STATE_DISCONNECTED -> {
                mConnection = null
                callback.onConnectStateChanged(ConnectionState.DISCONNECTED, device.address)
            }
            else -> {}
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            message("onServiceAdded: {uuid=${service!!.uuid}}")
        }
    }

    
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        mGattServer?.sendResponse(
            device,
            requestId,
            BluetoothGatt.GATT_SUCCESS,
            offset,
            characteristic!!.value
        )
    }

    
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        if (responseNeeded) {
            mGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
        callback.onMessage(device!!.address, characteristic.uuid, value!!, offset)
    }

    
    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor?
    ) {
        mGattServer?.sendResponse(
            device,
            requestId,
            BluetoothGatt.GATT_SUCCESS,
            offset,
            descriptor?.value
        )
    }

    
    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        if (responseNeeded) {
            mGattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                descriptor!!.value
            )
        }
    }

    
    override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
        debug("onExecuteWrite: { device=${device?.address}, requestId=$requestId, execute=$execute }")
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        debug("onNotificationSent: { device=${device?.address}, status=$status }")
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        debug("onMtuChanged: { device=${device?.address}, size=$mtu }")
        callback.onMtuChanged(device!!.address, mtu)
    }

    override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {

    }

    override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {

    }

    fun isConnected(address: String?): Boolean {
        return if (address == null) {
            mState == ConnectionState.CONNECTED
        } else {
            mState == ConnectionState.CONNECTED && address == mConnection!!.address
        }
    }

    fun disconnect() {
        if (mConnection != null) {
            mGattServer?.cancelConnection(mConnection)
            mConnection = null
        }
    }

    
    fun writeBytes(bytes: ByteArray): Boolean {
        if (mCharacteristicsForNotification.isEmpty()) return false
        return writeBytes(mCharacteristicsForNotification.first(), bytes)
    }

    fun writeBytes(characteristicUuid: UUID, bytes: ByteArray): Boolean {
        val characteristic =
            mCharacteristicsForNotification.find { it.uuid == characteristicUuid } ?: return false
        return writeBytes(characteristic, bytes)
    }
    
    private fun writeBytes(characteristic: BluetoothGattCharacteristic, bytes: ByteArray): Boolean {
        val device = mConnection ?: return false
        val indicate = characteristic
            .properties and BluetoothGattCharacteristic.PROPERTY_INDICATE == BluetoothGattCharacteristic.PROPERTY_INDICATE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mGattServer?.notifyCharacteristicChanged(
                device,
                characteristic,
                indicate,
                bytes
            )
        } else {
            characteristic.value = bytes
            mGattServer?.notifyCharacteristicChanged(
                device,
                characteristic,
                indicate
            )
        }
        return true
    }
}