package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import com.outlook.wn123o.blekit.BleEnv
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.common.findCharacteristic
import com.outlook.wn123o.blekit.common.hasProperties
import com.outlook.wn123o.blekit.common.message
import com.outlook.wn123o.blekit.common.runAtDelayed
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import java.io.Closeable

@SuppressLint("MissingPermission")
internal class BleGattCallbackImpl(
    mCtx: Context,
    private val mRemote: BluetoothDevice,
    private val mCallback: BleCentralCallback
) : BluetoothGattCallback(), Closeable {

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
        debug("onConnectionStateChange: { state=$status, newState=$newState }")
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                gatt?.let {
                    mBluetoothGatt = gatt
                    mCallback.onConnected(mRemote.address)
                    it.discoverServices()
                    it.requestMtu(BleEnv.expectMtuSize)
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
            val bleAddress = bluetoothGatt.device.address
            val gattService = findGattService(bluetoothGatt)
            if (gattService != null) {
                message("Use gatt service: { uuid=${gattService.uuid}, type=${gattService.type} }")
                mNotifyCharacteristic = findNotifyTypeCharacteristic(gattService)
                    ?.also { characteristic ->
                        message("Notify type characteristic: { uuid=${characteristic.uuid} }")
                        bluetoothGatt.setCharacteristicNotification(characteristic, true)
                        characteristic.getDescriptor(BleEnv.clientChaConfigUuid)
                            ?.let { descriptor ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    bluetoothGatt.writeDescriptor(
                                        descriptor,
                                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    )
                                } else {
                                    descriptor.value =
                                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    bluetoothGatt.writeDescriptor(descriptor)
                                }
                            }
                    }
                mWriteCharacteristic = findWriteTypeCharacteristic(gattService)
                    ?.also { characteristic ->
                        message("Write type characteristic: { uuid=${characteristic.uuid} }")
                        runAtDelayed(200L) {
                            mCallback.onReadyToWrite(bleAddress)
                        }
                    }
            } else {
                error("Gatt service not found: { uuid=${BleEnv.preferenceServiceUuid} }")
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
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mCallback.onMtuChanged(gatt!!.device.address, mtu)
        }
    }

    @SuppressLint("MissingPermission")
    override fun close() {
        mBluetoothGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
        }
    }

    fun writeBytes(bytes: ByteArray): Boolean {
        mBluetoothGatt?.let { gatt ->
            mWriteCharacteristic?.let { characteristic ->
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
        return mBluetoothGatt != null && mWriteCharacteristic != null
    }

    fun readRemoteRssi() {
        mBluetoothGatt?.readRemoteRssi()
    }


    /**
     * Search bluetooth gatt service.
     */
    private fun findGattService(bluetoothGatt: BluetoothGatt): BluetoothGattService? {
        val gattService = bluetoothGatt.getService(BleEnv.preferenceServiceUuid)
            ?: bluetoothGatt.services.find { gattService ->
                gattService.findCharacteristic(
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY
                ) != null && gattService.findCharacteristic(BluetoothGattCharacteristic.PROPERTY_WRITE) != null
            }
        if (gattService == null) {
            error("BluetoothGatt service not found! { address= ${bluetoothGatt.device.address}, service=${BleEnv.preferenceServiceUuid} }")
        }
        return gattService
    }

    /**
     * Search notify type characteristic.
     */
    private fun findNotifyTypeCharacteristic(gattService: BluetoothGattService): BluetoothGattCharacteristic? {
        val characteristic =
            gattService.getCharacteristic(BleEnv.preferenceNotifyChaUuid) ?: gattService.characteristics.find {
                it.hasProperties(
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY
                )
            }
        if (characteristic == null) {
            mCallback.onError(BleCentral.ERR_NOTIFY_CHARACTERISTIC_NOT_FOUND)
            error("Notify characteristic not found! { uuid=${BleEnv.preferenceNotifyChaUuid} }")
        }
        return characteristic
    }

    /**
     * Search write type characteristic.
     */
    private fun findWriteTypeCharacteristic(gattService: BluetoothGattService): BluetoothGattCharacteristic? {
        val characteristic =
            gattService.getCharacteristic(BleEnv.preferenceWriteChaUuid) ?: gattService.characteristics.find {
                it.hasProperties(
                    BluetoothGattCharacteristic.PROPERTY_WRITE
                )
            }
        if (characteristic == null) {
            mCallback.onError(BleCentral.ERR_WRITE_CHARACTERISTIC_NOT_FOUND)
            error("Write characteristic not found! { uuid=${BleEnv.preferenceWriteChaUuid} }")
        }
        return characteristic
    }
}