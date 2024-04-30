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
import com.outlook.wn123o.blekit.Env
import com.outlook.wn123o.blekit.interfaces.BlePeripheralApi
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback

@SuppressLint("MissingPermission")
class BlePeripheral(private var mExternCallback: BlePeripheralCallback? = null): AdvertiseCallback(), BlePeripheralCallback, BlePeripheralApi {
    private val mCtx = Env.context
    private val mAdapter: BluetoothAdapter
    private var mGattServer: BluetoothGattServer
    private val mGattCallback: BleGattServerCallbackImpl
    private var mAdvertising = false
    private val mGattService: BluetoothGattService

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
        mAdvertising = true
        val service = mGattServer
            .getService(Env.preferenceServiceUuid)
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
            Env.preferenceServiceUuid,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        service.addCharacteristic(mGattCallback.getWritableCharacteristic())
        service.addCharacteristic(mGattCallback.getNotifyCharacteristic())
        return service
    }

    override fun writeBytes(bleAddress: String, bytes: ByteArray): Boolean {
        return mGattCallback.writeBytes(bleAddress, bytes)
    }

    override fun startup() = startup(null)

    override fun startup(advertiseData: AdvertiseData?) {
        startAdvertising(advertiseData ?: Env.advertiseData)
    }

    override fun shutdown() {
        if (mAdvertising) stopAdvertising()
        mGattCallback.disconnectAll()
        mGattServer.clearServices()
    }

    override fun disconnect(bleAddress: String) {
        mGattCallback
            .disconnect(bleAddress)
    }

    override fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int) {
        mExternCallback?.onMessage(bleAddress, bytes, offset)
    }

    override fun onConnected(bleAddress: String) {
        stopAdvertising()
        mExternCallback?.onConnected(bleAddress)
    }

    override fun onDisconnected(bleAddress: String) {
        mExternCallback?.onDisconnected(bleAddress)
    }

    private fun startAdvertising(advertiseData: AdvertiseData) {
        if (!mAdvertising) {
            mAdapter
                .bluetoothLeAdvertiser
                .startAdvertising(
                    Env.advertiseSettings,
                    advertiseData,
                    this
                )
        }
    }

    private fun stopAdvertising() {
        if (mAdvertising) {
            mAdapter
                .bluetoothLeAdvertiser
                .stopAdvertising(this)
            mAdvertising = false
        }
    }

    override fun setPeripheralCallback(callback: BlePeripheralCallback) {
        mExternCallback = callback
    }

    companion object {
        const val ERR_ADVERTISE_FAILED = -1
    }
}