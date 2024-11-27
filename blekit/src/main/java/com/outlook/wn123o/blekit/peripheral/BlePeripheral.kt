package com.outlook.wn123o.blekit.peripheral
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import com.outlook.wn123o.blekit.BleEnvironment
import com.outlook.wn123o.blekit.common.advertiser.BleAdvertiser
import com.outlook.wn123o.blekit.common.error
import com.outlook.wn123o.blekit.common.runAtDelayed
import com.outlook.wn123o.blekit.common.runOnUiThread
import com.outlook.wn123o.blekit.interfaces.BlePeripheralApi
import com.outlook.wn123o.blekit.interfaces.BlePeripheralEventListener
import com.outlook.wn123o.blekit.interfaces.ConnectionState
import java.util.UUID

@SuppressLint("MissingPermission")
class BlePeripheral(private var mExternCallback: BlePeripheralCallback? = null): AdvertiseCallback(), BlePeripheralEventListener, BlePeripheralApi {
    private val mCtx = BleEnvironment.applicationContext
    private val mAdapter: BluetoothAdapter

    private val mGattCallback: BleGattServerCallbackImpl
    private val mLeAdvertiser = BleAdvertiser(this)

    init {
        val bleManager = mCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mAdapter = bleManager.adapter
        mGattCallback = BleGattServerCallbackImpl()
        mGattCallback.callback = this
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        super.onStartSuccess(settingsInEffect)
    }

    override fun onStartFailure(errorCode: Int) {
        mExternCallback?.onError(ERR_ADVERTISE_FAILED)
        error("Unable start advertise, code=$errorCode")
    }

    override fun startup(
        manufacturerId: Int?,
        manufacturerData: ByteArray?,
        dataServiceUUID: UUID?,
        dataServiceData: ByteArray?
    ) {
        val advertiseDataBuilder = AdvertiseData.Builder()
            .setIncludeDeviceName(
                BleEnvironment.advertiseFeatureIncludeDeviceName
            )
            .addServiceUuid(ParcelUuid(BleEnvironment.uuidForAdvertise))
        if (manufacturerId != null && manufacturerData != null) {
            advertiseDataBuilder.addManufacturerData(manufacturerId, manufacturerData)
        }
        if (dataServiceUUID != null && dataServiceData != null) {
            advertiseDataBuilder.addServiceData(ParcelUuid(dataServiceUUID), dataServiceData)
        }
        startup(advertiseDataBuilder.build())
    }

    override fun startup() = startup(null, null, null, null)

    override fun startup(advertiseData: AdvertiseData) = mLeAdvertiser.startAdvertising(advertiseData)

    override fun shutdown() {
        if (mLeAdvertiser.isAdvertising()) {
            mLeAdvertiser.stopAdvertising()
        }
        mGattCallback.disconnect()
        mGattCallback.releaseGatt()
    }

    override fun disconnect() = mGattCallback.disconnect()

    override fun writeBytes(bytes: ByteArray): Boolean = mGattCallback.writeBytes(bytes)

    override fun writeBytes(characteristic: UUID, bytes: ByteArray): Boolean = mGattCallback.writeBytes(characteristic, bytes)

    override fun onMessage(
        address: String,

        characteristic: UUID,
        bytes: ByteArray,
        offset: Int
    ) {
        runOnUiThread {
            mExternCallback?.onMessage(address, characteristic, bytes, offset)
        }
    }

    override fun onNotificationSent(bleAddress: String, success: Boolean) {
        runOnUiThread {
            mExternCallback?.onNotificationSent(bleAddress, success)
        }
    }

    override fun onConnectionStateChanged(@ConnectionState state: Int, address: String) {
        runOnUiThread {
            mExternCallback?.onConnectionStateChanged(state, address)
            if (state == ConnectionState.CONNECTED) {
                stopAdvertising()
                mExternCallback?.onConnected(address)
                runAtDelayed(200L) {
                    mExternCallback?.onReadyToWrite(address)
                }
            } else if (state == ConnectionState.DISCONNECTED) {
                mExternCallback?.onDisconnected(address)
            }
        }
    }

    override fun onMtuChanged(bleAddress: String, mtu: Int) {
        super.onMtuChanged(bleAddress, mtu)
        runOnUiThread {
            mExternCallback?.onMtuChanged(bleAddress, mtu)
        }
    }

    private fun stopAdvertising() = mLeAdvertiser.stopAdvertising()

    override fun registerCallback(callback: BlePeripheralCallback) {
        mExternCallback = callback
    }

    override fun unregisterCallback() {
        mExternCallback = null
    }

    override fun isConnected(address: String?): Boolean = mGattCallback.isConnected(address)

    companion object {
        const val ERR_ADVERTISE_FAILED = -1
    }
}