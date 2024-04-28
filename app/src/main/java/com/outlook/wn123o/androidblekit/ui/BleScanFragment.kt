package com.outlook.wn123o.androidblekit.ui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.os.Bundle
import android.os.ParcelUuid
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.outlook.wn123o.androidblekit.MainActivityViewModel
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.databinding.FragmentBleScanBinding
import com.outlook.wn123o.androidblekit.databinding.ItemDeviceViewBinding
import com.outlook.wn123o.blekit.BleKitScope
import com.outlook.wn123o.blekit.central.BleScanCallback
import com.outlook.wn123o.blekit.common.BleDevice

class BleScanFragment : Fragment() {

    private val binding by lazy {
        FragmentBleScanBinding.inflate(layoutInflater)
    }

    private val bleCentral by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java].getBleCentral()
    }

    private val mBleScanCallback = BleScanCallbackImpl()

    private val mDeviceList = mutableListOf<BleDevice>()
    private val mAdapter = ItemDeviceViewAdapter(mDeviceList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
        binding.recyclerView.adapter = mAdapter
        setupActions()
    }

    private fun setupActions() {
        binding.scanButton.setOnClickListener { startDiscovery() }
    }

    private inner class ItemDeviceViewHolder(private val itemBinding: ItemDeviceViewBinding): ViewHolder(itemBinding.root) {
        @SuppressLint("MissingPermission")
        fun bind(device: BleDevice) {
            itemBinding.addressTextView.text = device.bleAddress
            itemBinding.nameTextView.text = device.deviceName ?: "Unknown"
            itemBinding.rssiTextView.text = "${device.rssi}"
            itemBinding.connectButton.setOnClickListener {
                bleCentral.stopScan(mBleScanCallback)
                val arguments = Bundle()
                    .apply {
                        putParcelable(BleCentralFragment.KEY_PARAM_BLE_DEVICE, device.device)
                    }
                findNavController()
                    .navigate(R.id.navigation_central, arguments)
            }
        }
    }

    private inner class ItemDeviceViewAdapter(private val dataSource: List<BleDevice>): Adapter<ItemDeviceViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemDeviceViewHolder {
            return ItemDeviceViewHolder(
                ItemDeviceViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int = dataSource.size

        override fun onBindViewHolder(holder: ItemDeviceViewHolder, position: Int) {
            holder.bind(dataSource[position])
        }

    }

    private fun startDiscovery() {
        clearList()
        val filter = ScanFilter
            .Builder()
            .setServiceUuid(ParcelUuid(BleKitScope.getServiceUuid()))
            .build()
        bleCentral.scanWithDuration(5000, mBleScanCallback, listOf(filter))
    }

    private fun clearList() {
        if (mDeviceList.isNotEmpty()) {
            val count = mDeviceList.size
            mDeviceList.clear()
            mAdapter.notifyItemRangeRemoved(0, count)
        }
    }

    private inner class BleScanCallbackImpl: BleScanCallback() {
        override fun onScanResult(bleDevice: BleDevice) {
            val indexOf = mDeviceList.indexOf(bleDevice)
            if (indexOf != -1) {
                mDeviceList[indexOf].rssi = bleDevice.rssi
                mAdapter.notifyItemChanged(indexOf)
            } else {
                mDeviceList.add(bleDevice)
                mAdapter.notifyItemInserted(
                    mDeviceList.size
                )
            }
        }
    }
}