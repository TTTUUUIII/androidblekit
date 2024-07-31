package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.debug
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.common.runOnUiThread
import com.outlook.wn123o.blekit.interfaces.BleCentralApi
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import java.util.UUID

@SuppressLint("MissingPermission")
class BleCentral(private var mExternCallback: BleCentralCallback? = null): BleCentralCallback, BleCentralApi {
    private val mCtx = BleEnvironment.applicationContext

    private val mConnections = mutableMapOf<String, BleGattCallbackImpl>()

    
    override fun connect(device: BluetoothDevice) {
        debug("connect to ${device.address}")
        mConnections[device.address] = BleGattCallbackImpl(mCtx, device, this)
    }

    override fun onConnected(bleAddress: String) {
        runOnUiThread {
            mExternCallback?.onConnected(bleAddress)
        }
    }

    override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
        super.onMessage(address, characteristic, bytes, offset)
        runOnUiThread {
            mExternCallback?.onMessage(address, characteristic, bytes, offset)
        }
    }

    override fun onDisconnected(bleAddress: String) {
        mConnections.remove(bleAddress)
        runOnUiThread {
            mExternCallback?.onDisconnected(bleAddress)
        }
    }

    override fun onMtuChanged(bleAddress: String, mtu: Int) {
        runOnUiThread {
            mExternCallback?.onMtuChanged(bleAddress, mtu)
        }
    }

    override fun onReadyToWrite(bleAddress: String) {
        runOnUiThread {
            mExternCallback?.onReadyToWrite(bleAddress)
        }
    }

    override fun onReadRemoteRssi(bleAddress: String, rssi: Int) {
        runOnUiThread {
            mExternCallback?.onReadRemoteRssi(bleAddress, rssi)
        }
    }

    override fun onError(error: Int) {
        runOnUiThread {
            mExternCallback?.onError(error)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt) {
        runOnUiThread {
            mExternCallback?.onServicesDiscovered(gatt)
        }
    }

    
    override fun disconnect(bleAddress: String) {
        mConnections
            .remove(bleAddress)
            ?.close()
    }

    override fun writeBytes(address: String, bytes: ByteArray): Boolean {
        mConnections[address]
            ?.also {
                return it.writeBytes(bytes)
            }
        return false
    }

    override fun registerCallback(callback: BleCentralCallback) {
        mExternCallback = callback
    }

    override fun unregisterCallback() {
        mExternCallback = null
    }

    override fun readRemoteRssi(bleAddress: String): Boolean {
        mConnections[bleAddress]?.let {
            it.readRemoteRssi()
            return true
        }
        return false
    }

    
    companion object {
        @JvmStatic
        fun setCharacteristicNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, enable: Boolean): Boolean {
            val success =
                gatt.setCharacteristicNotification(characteristic, enable)
            val descriptorValue = if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            if (success) {
                characteristic.getDescriptor(BleEnvironment.notificationDescriptorUuid)
                    ?.let { descriptor ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(
                                descriptor,
                                descriptorValue
                            )
                        } else {
                            descriptor.value =
                                descriptorValue
                            gatt.writeDescriptor(descriptor)
                        }
                    }
            } else {
                error("Set ${characteristic.uuid} characteristic notification $enable failed!")
            }
            return success;
        }
    }
}