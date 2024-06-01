package com.outlook.wn123o.androidblekit.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.databinding.FragmentBleScanBinding
import com.outlook.wn123o.androidblekit.databinding.ItemDeviceViewBinding
import com.outlook.wn123o.blekit.common.BleDevice
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class BleScanFragment : Fragment() {

    private val binding by lazy {
        FragmentBleScanBinding.inflate(layoutInflater)
            .apply {
                viewModel = mViewModel
            }
    }

    private val mViewModel by lazy {
        ViewModelProvider(this)[BleScanFragmentViewModel::class.java]
    }

    private val mDeviceList = mutableListOf<BleDevice>()
    private val mAdapter = ItemDeviceViewAdapter(mDeviceList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.scanResultLiveData.observe(this) {
            it?.let { bleDevice ->
                onNewDevice(bleDevice)
            }
        }

        mViewModel.deviceNamePatternLiveData.observe(this) {
            mAdapter.notifyItemRangeChanged(0, mDeviceList.size)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
        binding.recyclerView.adapter = mAdapter
        clearList()
    }

    private inner class ItemDeviceViewHolder(private val itemBinding: ItemDeviceViewBinding): ViewHolder(itemBinding.root) {
        @SuppressLint("MissingPermission")
        fun bind(device: BleDevice) {
            itemBinding.bleDevice = device
            itemBinding.connectButton.setOnClickListener {
                mViewModel.stopDiscover()
                val arguments = Bundle()
                    .apply {
                        putParcelable(BleCentralFragment.KEY_ARG_BLE_DEVICE, device.device)
                        putInt(BleCentralFragment.KEY_ARG_BLE_RSSI, device.rssi)
                    }
                findNavController()
                    .navigate(R.id.navigation_central, arguments)
            }
            if (shouldDisplay(device)) {
                show()
            } else {
                hide()
            }
        }

        private fun hide() {
            itemBinding.root.updateLayoutParams<ViewGroup.LayoutParams> {
                height = 0
            }
        }

        private fun show() {
            itemBinding.root.updateLayoutParams<ViewGroup.LayoutParams> {
                height = LayoutParams.WRAP_CONTENT
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

    private fun clearList() {
        if (mDeviceList.isNotEmpty()) {
            val count = mDeviceList.size
            mDeviceList.clear()
            mAdapter.notifyItemRangeRemoved(0, count)
        }
    }

    private fun shouldDisplay(device: BleDevice): Boolean {
        try {
            val pattern = mViewModel.deviceNamePatternLiveData.value ?: ".*.*"
            if (pattern.isEmpty()) return true
            val deviceName = device.deviceName ?: ""
            return Pattern.matches(pattern, deviceName)
        } catch (ignored: PatternSyntaxException) { }
        return true
    }

    private fun onNewDevice(bleDevice: BleDevice) {
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