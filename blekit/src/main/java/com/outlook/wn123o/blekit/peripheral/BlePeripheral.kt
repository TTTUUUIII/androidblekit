package com.outlook.wn123o.blekit.peripheral
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
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
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback
import java.util.UUID

@SuppressLint("MissingPermission")
class BlePeripheral(private var mExternCallback: BlePeripheralCallback? = null): AdvertiseCallback(), BlePeripheralCallback, BlePeripheralApi {
    private val mCtx = BleEnvironment.applicationContext
    private val mAdapter: BluetoothAdapter
    private var mGattServer: BluetoothGattServer
    private val mGattCallback: BleGattServerCallbackImpl
    private val mGattService: BluetoothGattService
    private val mLeAdvertiser = BleAdvertiser(this)

    init {
        val bleManager = mCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mAdapter = bleManager.adapter
        mGattCallback = BleGattServerCallbackImpl()
        mGattCallback.callback = this
        mGattServer = bleManager.openGattServer(mCtx, mGattCallback)
        mGattCallback.gattServer = mGattServer
        mGattService = getGattService()
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        super.onStartSuccess(settingsInEffect)
        val service = mGattServer
            .getService(BleEnvironment.uuidForGattService)
        if (service == null) {
            mGattServer.addService(mGattService)
        }
    }

    override fun onStartFailure(errorCode: Int) {
        mExternCallback?.onError(ERR_ADVERTISE_FAILED)
        error("Unable start advertise, code=$errorCode")
    }

    private fun getGattService(): BluetoothGattService {
        val service = BluetoothGattService(
            BleEnvironment.uuidForGattService,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        mGattCallback.characteristicsForNotification.forEach(service::addCharacteristic)
        service.addCharacteristic(mGattCallback.characteristicForWritable)
        return service
    }

    override fun startup(
        manufacturerId: Int?,
        manufacturerData: ByteArray?,
        dataServiceUUID: UUID?,
        dataServiceData: ByteArray?
    ) {
        val advertiseDataBuilder = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(BleEnvironment.uuidForAdvertise))
        if (manufacturerId != null && manufacturerData != null) {
            advertiseDataBuilder.addManufacturerData(manufacturerId, manufacturerData)
        }
        if (dataServiceUUID != null && dataServiceData != null) {
            advertiseDataBuilder.addServiceData(ParcelUuid(dataServiceUUID), dataServiceData)
        }
        startup(advertiseDataBuilder.build())
    }

    override fun startup(advertiseData: AdvertiseData) = mLeAdvertiser.startAdvertising(advertiseData)

    override fun shutdown() {
        if (mLeAdvertiser.isAdvertising()) {
            mLeAdvertiser.stopAdvertising()
        }
        mGattCallback.disconnect()
        mGattServer.clearServices()
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

    override fun onConnected(bleAddress: String) {
        stopAdvertising()
        runOnUiThread {
            mExternCallback?.onConnected(bleAddress)
        }
        runAtDelayed(200L) {
            mExternCallback?.onReadyToWrite(bleAddress)
        }
    }

    override fun onDisconnected(bleAddress: String) {
        runOnUiThread {
            mExternCallback?.onDisconnected(bleAddress)
        }
    }

    private fun stopAdvertising() = mLeAdvertiser.stopAdvertising()

    override fun registerCallback(callback: BlePeripheralCallback) {
        mExternCallback = callback
    }

    override fun unregisterCallback() {
        mExternCallback = null
    }

    companion object {
        const val ERR_ADVERTISE_FAILED = -1
    }
}