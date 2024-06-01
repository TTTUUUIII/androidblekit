package com.outlook.wn123o.androidblekit.ui

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.outlook.wn123o.androidblekit.databinding.FragmentBleCentralBinding
import com.outlook.wn123o.androidblekit.databinding.MessageWindowViewBinding

class BleCentralFragment : Fragment() {

    private val mViewModel by lazy {
        ViewModelProvider(this)[BleCentralFragmentViewModel::class.java]
    }

    private val binding: FragmentBleCentralBinding by lazy {
        FragmentBleCentralBinding.inflate(layoutInflater)
    }

    private val messageBinding by lazy {
        MessageWindowViewBinding.inflate(layoutInflater, binding.messageWindow, true)
    }

    private var mBluetoothDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             arguments?.getParcelable(KEY_ARG_BLE_DEVICE, BluetoothDevice::class.java)
        } else {
            arguments?.getParcelable(KEY_ARG_BLE_DEVICE) as? BluetoothDevice
        }

        mBluetoothDevice?.let { bluetoothDevice ->
            mViewModel.updateRemoteRssiState(arguments?.getInt(KEY_ARG_BLE_RSSI) ?: 0)
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
    }

    override fun onStart() {
        super.onStart()
        mBluetoothDevice?.let { bluetoothDevice ->
            mViewModel.connect(bluetoothDevice)
        }
    }

    override fun onStop() {
        super.onStop()
        mViewModel.disconnect()
    }

    companion object {
        const val KEY_ARG_BLE_DEVICE = "ble_device"
        const val KEY_ARG_BLE_RSSI = "ble_rssi"
    }
}