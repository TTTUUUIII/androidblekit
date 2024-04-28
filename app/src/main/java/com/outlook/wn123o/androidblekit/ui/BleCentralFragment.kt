package com.outlook.wn123o.androidblekit.ui

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.outlook.wn123o.androidblekit.MainActivityViewModel
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.toast
import com.outlook.wn123o.androidblekit.databinding.FragmentBleCentralBinding
import com.outlook.wn123o.androidblekit.databinding.MessageWindowViewBinding
import com.outlook.wn123o.blekit.interfaces.BleCentralCallback

class BleCentralFragment : Fragment(), BleCentralCallback {

    private val mViewModel by lazy {
        ViewModelProvider(this)[BleCentralFragmentViewModel::class.java]
    }

    private val binding: FragmentBleCentralBinding by lazy {
        FragmentBleCentralBinding.inflate(layoutInflater)
    }

    private val messageBinding by lazy {
        MessageWindowViewBinding.inflate(layoutInflater, binding.messageWindow, true)
    }

    private val bleCentral by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java].getBleCentral(this)
    }

    private var mBluetoothDevice: BluetoothDevice? = null

    private var mConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             arguments?.getParcelable(KEY_PARAM_BLE_DEVICE, BluetoothDevice::class.java)
        } else {
            arguments?.getParcelable(KEY_PARAM_BLE_DEVICE) as? BluetoothDevice
        }

        if (mBluetoothDevice != null) {
            bleCentral.connect(mBluetoothDevice!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = mViewModel
        binding.lifecycleOwner = this
        messageBinding.viewModel = mViewModel
        messageBinding.lifecycleOwner = this
        setupActions()
    }

    private fun setupActions() {
        messageBinding.sendMsgButton.setOnClickListener {
            if (mViewModel.txMsg.isNotEmpty() && mConnected) {
                bleCentral.send(mBluetoothDevice!!.address, mViewModel.txMsg.encodeToByteArray())
            } else {
                toast(R.string.str_send_failure)
            }
        }
    }

    override fun onConnected(bleAddress: String) {
        mViewModel.updateConnectState("已连接")
        mConnected = true
    }

    override fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int) {
        mViewModel.putMsg("$bleAddress: ${String(bytes)}")
    }

    override fun onDisconnected(bleAddress: String) {
        mViewModel.updateConnectState("未连接")
        mConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mConnected) {
            bleCentral.disconnect(mBluetoothDevice!!.address)
        }
    }

    companion object {
        const val KEY_PARAM_BLE_DEVICE = "ble_device"
    }
}