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
import com.outlook.wn123o.blekit.Env
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.message
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import java.util.Arrays

internal class BleGattServerCallbackImpl() : BluetoothGattServerCallback() {

    lateinit var gattServer: BluetoothGattServer
    lateinit var callback: BlePeripheralCallback

    private val mWriteCharacteristic = BluetoothGattCharacteristic(
        Env.preferenceWriteChaUuid,
        BluetoothGattCharacteristic.PROPERTY_WRITE
               /* or BluetoothGattCharacteristic.PROPERTY_READ
                or BluetoothGattCharacteristic.PROPERTY_INDICATE*/,
        /*BluetoothGattCharacteristic.PERMISSION_READ
                or*/ BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    private val mNotifyCharacteristic = BluetoothGattCharacteristic(
        Env.preferenceNotifyChaUuid,
        BluetoothGattCharacteristic.PROPERTY_READ
                or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ
    ).apply {
        addDescriptor(Env.clientChaDescriptor)
    }

    private val mConnections = mutableMapOf<String, BluetoothDevice>()

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        debug("onConnectionStateChange: {state=$status, newState=$newState}")
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                device?.let { bluetoothDevice ->
                    mConnections[bluetoothDevice.address] = bluetoothDevice
                    gattServer.connect(bluetoothDevice, false)
                    callback.onConnected(bluetoothDevice.address)
                }
            }

            BluetoothGatt.STATE_DISCONNECTED -> {
                device?.let { bluetoothDevice ->
                    mConnections.remove(bluetoothDevice.address)
                    callback.onDisconnected(bluetoothDevice.address)
                }
            }

            else -> {}
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        message("Gatt service added")
    }

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        if (responseNeeded) {
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
        callback.onMessage(device!!.address, value!!, offset)
    }

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
    fun disconnect(bleAddress: String) {
        mConnections
            .remove(bleAddress)
            ?.let {
                gattServer.cancelConnection(it)
            }
    }

    fun disconnectAll() {
        mConnections.keys.forEach { bleAddress ->
            disconnect(bleAddress)
        }
    }

    @SuppressLint("MissingPermission")
    fun writeBytes(bleAddress: String, bytes: ByteArray): Boolean {
        mConnections[bleAddress]
            ?.let { device ->
                val characteristic = mNotifyCharacteristic
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
        return false
    }

    fun getWritableCharacteristic() = mWriteCharacteristic

    fun getNotifyCharacteristic() = mNotifyCharacteristic
}