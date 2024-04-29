package com.outlook.wn123o.androidblekit.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.outlook.wn123o.androidblekit.MainActivityViewModel
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.common.runOnUiThread
import com.outlook.wn123o.androidblekit.common.timeZone
import com.outlook.wn123o.androidblekit.common.toast
import com.outlook.wn123o.androidblekit.databinding.FragmentBlePeripheralBinding
import com.outlook.wn123o.androidblekit.databinding.MessageWindowViewBinding
import com.outlook.wn123o.blekit.interfaces.BlePeripheralCallback

class BlePeripheralFragment : Fragment(), BlePeripheralCallback {

    private val binding by lazy {
        FragmentBlePeripheralBinding.inflate(layoutInflater)
    }

    private val messageBinding: MessageWindowViewBinding by lazy {
        MessageWindowViewBinding.inflate(layoutInflater, binding.messageWindow, true)
    }

    private val mViewModel by lazy {
        ViewModelProvider(this)[BlePeripheralFragmentViewModel::class.java]
    }

    private val blePeripheral by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java].getBlePeripheral(this)
    }

    private var mConnected = false
    private var mBleAddress: String? = null

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
        startAdvertising()
    }

    private fun setupActions() {
        messageBinding.sendMsgButton.setOnClickListener {
            if (mViewModel.txMsg.isNotEmpty() && mConnected) {
                blePeripheral.writeBytes(mBleAddress!!, mViewModel.txMsg.encodeToByteArray())
            } else {
                toast(R.string.str_send_failure)
            }
        }
    }

    override fun onMessage(bleAddress: String, bytes: ByteArray, offset: Int) {
        mViewModel.putMsg("${timeZone()}: ${String(bytes)}")
    }

    override fun onConnected(bleAddress: String) {
        mViewModel.updateConnectState(getString(R.string.str_connected))
        mViewModel.updateRemoteAddressState(bleAddress)
        mBleAddress = bleAddress
        mConnected = true
    }

    override fun onDisconnected(bleAddress: String) {
        mViewModel.updateConnectState(getString(R.string.str_disconnected))
        mViewModel.updateRemoteAddressState("")
        mConnected = false
        startAdvertising()
        runOnUiThread {
            toast(R.string.str_connection_lost)
        }
    }

    private fun startAdvertising() {
        blePeripheral.startup()
    }

    override fun onDestroy() {
        super.onDestroy()
        blePeripheral.shutdown()
    }
}