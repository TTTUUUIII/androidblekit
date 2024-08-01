package com.outlook.wn123o.blekit.central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.common.runOnUiThread
import com.outlook.wn123o.blekit.common.warn
import com.outlook.wn123o.blekit.interfaces.BleCentralApi
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import java.util.UUID

@SuppressLint("MissingPermission")
class BleCentral(private var mExternCallback: BleCentralCallback? = null): BleCentralCallback, BleCentralApi {
    private val mCtx = BleEnvironment.applicationContext

    private val mConnections = mutableMapOf<String, BleGattCallbackImpl>()
    
    override fun connect(device: BluetoothDevice) {
        if (mConnections[device.address] == null) {
            mConnections[device.address] = BleGattCallbackImpl(mCtx, device, this)
        } else {
            warn("Already connected to ${device.address}!")
        }
    }

    override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
        super.onMessage(address, characteristic, bytes, offset)
        runOnUiThread {
            mExternCallback?.onMessage(address, characteristic, bytes, offset)
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

    override fun onError(error: Int, address: String?) {
        if (error == ERR_CONNECT_FAILED) {
            mConnections.remove(address)
            error("Unable connect to $address")
        }
        runOnUiThread {
            mExternCallback?.onError(error, address)
        }
    }

    override fun onCharacteristicRegistered(characteristic: UUID, notify: Boolean) {
        runOnUiThread {
            mExternCallback?.onCharacteristicRegistered(characteristic, notify)
        }
    }

    override fun onConnectionStateChanged(@ConnectionState state: Int, address: String) {
        runOnUiThread {
            mExternCallback?.onConnectionStateChanged(state, address)
            if (state == ConnectionState.CONNECTED) {
                mExternCallback?.onConnected(address)
            } else if (state == ConnectionState.DISCONNECTED) {
                mConnections.remove(address)
                mExternCallback?.onDisconnected(address)
            }
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

    override fun writeBytes(address: String, data: ByteArray): Boolean = writeBytes(address, data, null)

    override fun writeBytes(address: String, data: ByteArray, characteristic: UUID?): Boolean {
        mConnections[address]
            ?.also {
                return it.writeBytes(data, characteristic)
            }
        return false
    }

    override fun registerCallback(callback: BleCentralCallback) {
        mExternCallback = callback
    }

    override fun unregisterCallback() {
        mExternCallback = null
    }

    override fun isConnected(address: String?): Boolean {
        if (address == null) {
            if (mConnections.isEmpty()) return false
            return mConnections.values.first().isConnected()
        } else {
            val impl = mConnections[address] ?: return false
            return impl.isConnected()
        }
    }

    override fun readRemoteRssi(bleAddress: String): Boolean {
        mConnections[bleAddress]?.let {
            it.readRemoteRssi()
            return true
        }
        return false
    }

    
    companion object {

        const val ERR_CONNECT_FAILED = 1

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