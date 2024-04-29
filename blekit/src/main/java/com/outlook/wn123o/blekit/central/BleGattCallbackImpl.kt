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
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import java.io.Closeable

@SuppressLint("MissingPermission")
internal class BleGattCallbackImpl(mCtx: Context, private val mRemote: BluetoothDevice, private val mCallback: BleCentralCallback): BluetoothGattCallback(), Closeable {

    private var mBluetoothGatt: BluetoothGatt? = null
    private var mWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

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
                    mBluetoothGatt = gatt
                    mCallback.onConnected(mRemote.address)
                    it.requestMtu(Env.expectMtuSize)
                }
            }
            BluetoothGatt.STATE_DISCONNECTED -> {
                mCallback.onDisconnected(mRemote.address)
                mBluetoothGatt = null
            }
            else -> {}
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        gatt?.let { bluetoothGatt ->
            val gattService = bluetoothGatt.getService(Env.serviceUuid)
            val bleAddress = bluetoothGatt.device.address
            if (gattService != null) {
                mNotifyCharacteristic = gattService.getCharacteristic(Env.notifyChaUuid)
                if (mNotifyCharacteristic != null) {
                    bluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic, true)
                    mNotifyCharacteristic!!.getDescriptor(Env.clientChaConfigUuid)
                        ?.let { descriptor ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                bluetoothGatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            } else {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                bluetoothGatt.writeDescriptor(descriptor)
                            }
                        }
                } else {
                    mCallback.onError(BleCentral.ERR_NOTIFY_CHARACTERISTIC_NOT_FOUND)
                    error("Notify characteristic not found! { address= ${bleAddress}, service=${Env.notifyChaUuid} }")
                }
                mWriteCharacteristic = gattService.getCharacteristic(Env.writeChaUuid)
                if (mWriteCharacteristic != null) {
                    mCallback.onReadyToWrite(bleAddress)
                } else {
                    mCallback.onError(BleCentral.ERR_WRITE_CHARACTERISTIC_NOT_FOUND)
                    error("Write characteristic not found! { address= ${bleAddress}, service=${Env.writeChaUuid} }")
                }
            } else {
                error("BluetoothGatt service not found! { address= ${bleAddress}, service=${Env.serviceUuid} }")
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        debug("onCharacteristicRead: { status=$status }")
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        debug("onCharacteristicWrite: { status=$status }")
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
        debug("onDescriptorRead: { status=$status }")
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        debug("onDescriptorWrite: { status=$status }")
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        debug("onReliableWriteCompleted: { status=$status }")
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mCallback.onReadRemoteRssi(gatt!!.device.address, rssi)
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        gatt?.discoverServices()
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mCallback.onMtuChanged(gatt!!.device.address, mtu)
        }
    }

    @SuppressLint("MissingPermission")
    override fun close() {
        mBluetoothGatt?.let {  gatt ->
            gatt.disconnect()
            gatt.close()
        }
    }

    fun writeBytes(bytes: ByteArray): Boolean {
        mBluetoothGatt?.let {  gatt ->
            mWriteCharacteristic?.let { characteristic ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(
                        characteristic,
                        bytes,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                } else {
                    characteristic.value = bytes
                    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    gatt.writeCharacteristic(characteristic)
                }
            }
        }
        return mBluetoothGatt != null && mWriteCharacteristic != null
    }

    fun readRemoteRssi() {
        mBluetoothGatt?.readRemoteRssi()
    }
}