package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import com.outlook.wn123o.blekit.Env
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import java.io.Closeable

@SuppressLint("MissingPermission")
class BleGattCallbackImpl(private val mCtx: Context, private val mRemote: BluetoothDevice, private val mCallback: BleCentralCallback): BluetoothGattCallback(), Closeable {

    private var mGatt: BluetoothGatt? = null
    private lateinit var mWriteCharacteristic: BluetoothGattCharacteristic
    private lateinit var mNotifyCharacteristic: BluetoothGattCharacteristic

    init {
        mRemote.connectGatt(mCtx, false, this)
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        debug("onConnectionStateChange: {state=$status, newState=$newState}")
        when(newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                gatt?.let {
                    mGatt = gatt
                    mCallback.onConnected(mRemote.address)
                    it.requestMtu(Env.expectMtuSize)
                }
            }
            BluetoothGatt.STATE_DISCONNECTED -> {
                mCallback.onDisconnected(mRemote.address)
                mGatt = null
            }
            else -> {}
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        gatt?.let { bluetoothGatt ->
            val gattService = bluetoothGatt.getService(Env.serviceUuid)
            if (gattService != null) {
                mNotifyCharacteristic = gattService.getCharacteristic(Env.notifyChaUuid)
                bluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic, true)
                mNotifyCharacteristic.getDescriptor(Env.clientChaConfigUuid)
                    ?.let { descriptor ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bluetoothGatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            bluetoothGatt.writeDescriptor(descriptor)
                        }
                    }
                mWriteCharacteristic = gattService.getCharacteristic(Env.writeChaUuid)
            } else {
                error("Unable find gatt service: ${Env.serviceUuid}")
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        debug("onCharacteristicRead { status=$status }")
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        debug("onCharacteristicWrite")
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        mCallback.onMessage(gatt.device.address, value, 0)
    }


    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray
    ) {
        super.onDescriptorRead(gatt, descriptor, status, value)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        gatt?.discoverServices()
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mCallback.onMtuChanged(gatt!!.device.address, mtu)
        }
    }

    @SuppressLint("MissingPermission")
    override fun close() {
        mGatt?.let {  gatt ->
            gatt.disconnect()
            gatt.close()
        }
    }

    fun send(bytes: ByteArray): Boolean {
        mGatt?.let {  gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                    mWriteCharacteristic,
                    bytes,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                mWriteCharacteristic.value = bytes
                mWriteCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                gatt.writeCharacteristic(mWriteCharacteristic)
            }
        }
        return mGatt != null
    }
}