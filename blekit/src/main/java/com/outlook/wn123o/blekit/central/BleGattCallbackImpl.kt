package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.hasProperty
import com.outlook.wn123o.blekit.common.message
import com.outlook.wn123o.blekit.common.runAtDelayed
import com.outlook.wn123o.blekit.common.warn
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import java.io.Closeable

@SuppressLint("MissingPermission")
internal class BleGattCallbackImpl(
    context: Context,
    private val mRemote: BluetoothDevice,
    private val mCallback: BleCentralCallback
) : BluetoothGattCallback(), Closeable {

    private var mGatt: BluetoothGatt? = null
    private var mWritableCharacteristic: BluetoothGattCharacteristic? = null

    init {
        mRemote.connectGatt(context, false, this)
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        debug("onConnectionStateChange: { state=$status, newState=$newState }")
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                gatt?.let {
                    mGatt = gatt
                    mCallback.onConnected(mRemote.address)
                    it.discoverServices()
                    if (BleEnvironment.expectMtuSize >= 20) {
                        it.requestMtu(BleEnvironment.expectMtuSize)
                    }
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
        if (status == BluetoothGatt.GATT_SUCCESS) {
            val bleAddress = gatt!!.device.address
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristics ->
                    if (characteristics.uuid in BleEnvironment.uuidsForNotification) {
                        if (characteristics.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                            BleCentral.setCharacteristicNotification(gatt, characteristics, true)
                            message("Subscribed characteristics {uuid=${characteristics.uuid}}")
                        } else {
                            warn("Unable subscribe characteristics {uuid=${characteristics.uuid}}, because it is not a notification type!")
                        }
                    } else if (characteristics.uuid == BleEnvironment.uuidForWrite) {
                        mWritableCharacteristic = characteristics
                        message("Writable characteristics {uuid=${characteristics.uuid}}")
                        runAtDelayed(200L) {
                            mCallback.onReadyToWrite(bleAddress)
                        }
                    }
                }
            }
            mCallback.onServicesDiscovered(gatt)
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
        mCallback.onMessage(gatt.device.address, characteristic.uuid, value, 0)
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
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mCallback.onMtuChanged(gatt!!.device.address, mtu)
        }
    }

    @SuppressLint("MissingPermission")
    override fun close() {
        mGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            mGatt = null
        }
    }

    fun writeBytes(bytes: ByteArray): Boolean {
        mGatt?.let { gatt ->
            mWritableCharacteristic?.let { characteristic ->
                val writeType = if (
                    characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                ) {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                } else {
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(
                        characteristic,
                        bytes,
                        writeType
                    )
                } else {
                    characteristic.value = bytes
                    characteristic.writeType = writeType
                    gatt.writeCharacteristic(characteristic)
                }
            }
        }
        return mGatt != null && mWritableCharacteristic != null
    }

    fun readRemoteRssi() {
        mGatt?.readRemoteRssi()
    }
}