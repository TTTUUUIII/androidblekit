package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.BleHandler
import com.outlook.wn123o.blekit.common.BleMsg
import com.outlook.wn123o.blekit.common.BleMsgQueue
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.common.hasProperty
import com.outlook.wn123o.blekit.common.runAtDelayed
import com.outlook.wn123o.blekit.common.warn
import com.outlook.wn123o.blekit.interfaces.BleCentralEventListener
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import java.io.Closeable
import java.util.UUID

@SuppressLint("MissingPermission")
internal class BleGattCallbackImpl(
    context: Context,
    private val mRemote: BluetoothDevice,
    private val mCallback: BleCentralEventListener
) : BluetoothGattCallback(), Closeable {

    private var mGatt: BluetoothGatt? = null
    private val mWritableCharacteristics = mutableListOf<BluetoothGattCharacteristic>()
    private var waiting = true
    private var mState = ConnectionState.DISCONNECTED
    private var mMsgQueue = BleMsgQueue()

    init {
        mRemote.connectGatt(context, false, this)
        mCallback.onConnectionStateChanged(ConnectionState.CONNECTING, mRemote.address)
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        debug("onPhyUpdate: $txPhy, $rxPhy, $status")
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        debug("onPhyRead: $txPhy, $rxPhy, $status")
    }
    
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        debug("onConnectionStateChange: { state=${ConnectionState.string(status)}, newState=${ConnectionState.string(newState)} }")
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                if (isConnected()) {
                    warn("Already connected,")
                    return
                }
                mGatt = gatt;
                mCallback.onConnectionStateChanged(ConnectionState.CONNECTED, gatt.device.address)
                gatt.discoverServices()
                if (BleEnvironment.expectMtuSize >= 20) {
                    gatt.requestMtu(BleEnvironment.expectMtuSize)
                }
            }
            BluetoothGatt.STATE_DISCONNECTED -> {
                if (waiting) {
                    mCallback.onError(BleCentral.ERR_CONNECT_FAILED, mRemote.address)
                }
                mCallback.onConnectionStateChanged(ConnectionState.DISCONNECTED, gatt.device.address)
                mGatt = null
            }
            else -> {}
        }
        waiting = false
        mState = newState
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            val bleAddress = gatt!!.device.address
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (BleEnvironment.bleGattService.isExistInNotification(characteristic)) {
                        if (characteristic.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                            BleCentral.setCharacteristicNotification(gatt, characteristic, true)
                            mCallback.onCharacteristicRegistered(characteristic.uuid, true)
                            debug("Registered characteristic {uuid=${characteristic.uuid}, type=notify}")
                        } else {
                            warn("Unable register characteristic {uuid=${characteristic.uuid}}, because it is not a notification type!")
                        }
                    } else if (BleEnvironment.bleGattService.isExistInWritable(characteristic)) {
                        if (characteristic.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE) || characteristic.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) {
                            mWritableCharacteristics.add(characteristic)
                            mCallback.onCharacteristicRegistered(characteristic.uuid, false)
                            if (characteristic.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) {
                                debug("Registered characteristic {uuid=${characteristic.uuid}, type=writable_no_response}")
                            } else {
                                debug("Registered characteristic {uuid=${characteristic.uuid}, type=writable}")
                            }
                        } else {
                            warn("Unable register characteristics {uuid=${characteristic.uuid}}, because it is not a writable type!")
                        }
                    }
                }
            }
            if (mWritableCharacteristics.isNotEmpty()) {
                runAtDelayed(200L) {
                    mCallback.onReadyToWrite(bleAddress)
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
        mCallback.onCharacteristicWrite(characteristic!!.uuid, status == BluetoothGatt.GATT_SUCCESS)
        if (mMsgQueue.isNotEmpty()) {
            val msg = mMsgQueue.remove()
            writeCharacteristic(msg.characteristic, msg.data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            mMsgQueue.isActive = false
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        if (BleEnvironment.centralFeatureUseDeprecatedOnMessage) {
            mCallback.onMessage(gatt.device.address, characteristic.uuid, characteristic.value, 0)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        if (BleEnvironment.centralFeatureUseDeprecatedOnMessage) return
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
        if (status != BluetoothGatt.GATT_SUCCESS) {
            error("onDescriptorWrite: { status=$status }")
        }
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
    
    override fun close() {
        if (isConnected()) {
            try {
                mGatt!!.disconnect()
                mGatt!!.close()
                mGatt = null
            } catch (ignored: NullPointerException) {}
        }
    }

    fun requestMtu(mtu: Int) {
        mGatt?.requestMtu(mtu)
    }

    fun isConnected(): Boolean = mState == ConnectionState.CONNECTED

    fun writeBytes(data: ByteArray, characteristicUuid: UUID? = null): Boolean {
        if (!isConnected()) return false
        if (mWritableCharacteristics.isEmpty()) return false
        val characteristic = if (characteristicUuid == null) {
            mWritableCharacteristics.first()
        } else {
            mWritableCharacteristics.find { it.uuid == characteristicUuid }
        }
        if (characteristic == null) return false
        mGatt?.let { gatt ->
            val writeType = if (
                characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            ) {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            } else {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            if (writeType == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) {
                writeCharacteristic(characteristic, data, writeType)
            } else {
                if (!mMsgQueue.isActive) {
                    writeCharacteristic(characteristic, data, writeType)
                    mMsgQueue.isActive = true
                } else {
                    mMsgQueue.add(BleMsg(data, characteristic))
                }
            }
        }
        return mGatt != null
    }

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: ByteArray, writeType: Int) {
        mGatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                    characteristic,
                    data,
                    writeType
                )
            } else {
                characteristic.value = data
                characteristic.writeType = writeType
                gatt.writeCharacteristic(characteristic)
            }
        }
    }

    fun readRemoteRssi() {
        mGatt?.readRemoteRssi()
    }
}