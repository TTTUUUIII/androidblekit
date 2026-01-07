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
import com.outlook.wn123o.blekit.common.BleMsg
import com.outlook.wn123o.blekit.common.BleMsgQueue
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.info
import com.outlook.wn123o.blekit.common.message
import com.outlook.wn123o.blekit.common.warn
import com.outlook.wn123o.blekit.interfaces.BlePeripheralEventListener
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import java.util.UUID

@SuppressLint("MissingPermission")
internal class BleGattServerCallbackImpl() : BluetoothGattServerCallback() {

    private var mGattServer: BluetoothGattServer? = null
    lateinit var callback: BlePeripheralEventListener

    private val mCharacteristicsForNotification by lazy { BleEnvironment.bleGattService.characteristicsForNotification }

    private var mConnection: BluetoothDevice? = null
    private var mState = ConnectionState.DISCONNECTED
//    private val mMsgQueue = BleMsgQueue()

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
        when (newState) {
            BluetoothGatt.STATE_CONNECTING -> {
                callback.onConnectionStateChanged(ConnectionState.CONNECTING, device.address)
            }
            BluetoothGatt.STATE_CONNECTED -> {
                if (isConnected(device.address)) {
                    warn("Already connected,")
                    return
                }
                mGattServer!!.connect(device, false)
                callback.onConnectionStateChanged(ConnectionState.CONNECTED, device.address)
                mConnection = device
            }
            BluetoothGatt.STATE_DISCONNECTING -> {
                callback.onConnectionStateChanged(ConnectionState.DISCONNECTING, device.address)
            }
            BluetoothGatt.STATE_DISCONNECTED -> {
                mConnection = null
                callback.onConnectionStateChanged(ConnectionState.DISCONNECTED, device.address)
            }
            else -> {}
        }
        mState = newState
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
        callback.onNotificationSent(device!!.address, status == BluetoothGatt.GATT_SUCCESS)
//        if (mMsgQueue.isNotEmpty()) {
//            val msg = mMsgQueue.poll()
//            writeCharacteristic(msg!!.characteristic, msg.data)
//        } else {
//            mMsgQueue.isActive = false
//        }
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        debug("onMtuChanged: { device=${device?.address}, size=$mtu }")
        callback.onMtuChanged(device!!.address, mtu)
    }

    override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        info("onPhyUpdate: { device=${device?.address}, txPhy=$txPhy, rxPhy=$rxPhy, status=$status }")
    }

    override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        debug("onPhyRead: { device=${device?.address}, txPhy=$txPhy, rxPhy=$rxPhy, status=$status }")
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
        if (mConnection == null) return false
//        if (mMsgQueue.isActive) {
//            mMsgQueue.add(BleMsg(bytes, characteristic))
//        } else {
            writeCharacteristic(characteristic, bytes)
//            mMsgQueue.isActive = true
//        }
        return true
    }

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: ByteArray) {
        val indicate = characteristic
            .properties and BluetoothGattCharacteristic.PROPERTY_INDICATE == BluetoothGattCharacteristic.PROPERTY_INDICATE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mGattServer?.notifyCharacteristicChanged(
                mConnection!!,
                characteristic,
                indicate,
                data
            )
        } else {
            characteristic.value = data
            mGattServer?.notifyCharacteristicChanged(
                mConnection!!,
                characteristic,
                indicate
            )
        }
    }
}