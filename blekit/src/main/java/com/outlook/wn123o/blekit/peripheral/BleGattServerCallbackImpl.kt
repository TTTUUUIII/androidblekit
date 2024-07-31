package com.outlook.wn123o.blekit.peripheral

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.os.Build
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.message
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import java.util.Arrays
import java.util.UUID

@SuppressLint("MissingPermission")
internal class BleGattServerCallbackImpl() : BluetoothGattServerCallback() {

    lateinit var gattServer: BluetoothGattServer
    lateinit var callback: BlePeripheralCallback

    val characteristicForWritable = BluetoothGattCharacteristic(
        BleEnvironment.uuidForWrite,
        BluetoothGattCharacteristic.PROPERTY_WRITE
               /* or BluetoothGattCharacteristic.PROPERTY_READ
                or BluetoothGattCharacteristic.PROPERTY_INDICATE*/,
        /*BluetoothGattCharacteristic.PERMISSION_READ
                or*/ BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val characteristicsForNotification by lazy { BleEnvironment.buildCharacteristicsForNotification() }

    private var mConnection: BluetoothDevice? = null

    
    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        debug("onConnectionStateChange: {state=$status, newState=$newState}")
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                device?.let { bluetoothDevice ->
                    gattServer.connect(bluetoothDevice, true)
                    callback.onConnected(bluetoothDevice.address)
                    mConnection = bluetoothDevice
                }
            }

            BluetoothGatt.STATE_DISCONNECTED -> {
                device?.let { bluetoothDevice ->
                    mConnection = null
                    callback.onDisconnected(bluetoothDevice.address)
                }
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
        gattServer.sendResponse(
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
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
        callback.onMessage(device!!.address, characteristic.uuid, value!!, offset)
    }

    
    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor?
    ) {
        gattServer.sendResponse(
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
        if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
            descriptor!!.value = value
        }
        if (responseNeeded) {
            gattServer.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
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

    
    fun disconnect() {
        if (mConnection != null) {
            gattServer.cancelConnection(mConnection)
            mConnection = null
        }
    }

    
    fun writeBytes(bytes: ByteArray): Boolean {
        if (characteristicsForNotification.isEmpty()) return false
        return writeBytes(characteristicsForNotification.first(), bytes)
    }

    fun writeBytes(characteristicUuid: UUID, bytes: ByteArray): Boolean {
        val characteristic =
            characteristicsForNotification.find { it.uuid == characteristicUuid } ?: return false
        return writeBytes(characteristic, bytes)
    }

    
    private fun writeBytes(characteristic: BluetoothGattCharacteristic, bytes: ByteArray): Boolean {
        val device = mConnection ?: return false
        val indicate = characteristic
            .properties and BluetoothGattCharacteristic.PROPERTY_INDICATE == BluetoothGattCharacteristic.PROPERTY_INDICATE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gattServer.notifyCharacteristicChanged(
                device,
                characteristic,
                indicate,
                bytes
            )
        } else {
            characteristic.value = bytes
            gattServer.notifyCharacteristicChanged(
                device,
                characteristic,
                indicate
            )
        }
        return true
    }
}