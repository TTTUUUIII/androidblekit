package com.outlook.wn123o.androidblekit.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.outlook.wn123o.androidblekit.databinding.FragmentBlePeripheralBinding
import com.outlook.wn123o.androidblekit.databinding.MessageWindowViewBinding

class BlePeripheralFragment : Fragment() {

    private val binding by lazy {
        FragmentBlePeripheralBinding.inflate(layoutInflater)
    }

    private val messageBinding: MessageWindowViewBinding by lazy {
        MessageWindowViewBinding.inflate(layoutInflater, binding.messageWindow, true)
    }

    private val mViewModel by lazy {
        ViewModelProvider(this)[BlePeripheralFragmentViewModel::class.java]
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
        mViewModel.setBlePeripheralEnable(true)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.setBlePeripheralEnable(false)
    }
}