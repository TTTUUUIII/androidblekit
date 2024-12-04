package com.outlook.wn123o.androidblekit.ui

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.outlook.wn123o.androidblekit.common.Msg
import com.outlook.wn123o.androidblekit.common.MsgHistoryViewAdapter
import com.outlook.wn123o.androidblekit.common.getExtensionName
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
    private lateinit var startActivityForResult: ActivityResultLauncher<Intent>
    private val adapter = MsgHistoryViewAdapter()
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
        startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
            if (intent.resultCode == Activity.RESULT_OK) {
                intent.data?.data?.let {
                    val extension = getExtensionName(it)
                    requireActivity().contentResolver.openInputStream(it)?.let { inputStream ->
                        mViewModel.sendStream(inputStream, extension)
                    }
                }
            }
        }
        mViewModel.event.observe(this) { event ->
            if (event == BleCentralFragmentViewModel.EVENT_SELECT_SEND_FILE) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .apply {
                        type = "*/*"
                    }
                startActivityForResult.launch(intent)
            }
        }
        mBluetoothDevice?.let { bluetoothDevice ->
            mViewModel.connect(bluetoothDevice)
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
        messageBinding.adapter = adapter
        val listOf = listOf(Msg(Msg.TYPE_TEXT, "1111111111"))
        adapter.submitList(listOf)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.disconnect()
    }

    companion object {
        const val KEY_ARG_BLE_DEVICE = "ble_device"
        const val KEY_ARG_BLE_RSSI = "ble_rssi"
    }
}