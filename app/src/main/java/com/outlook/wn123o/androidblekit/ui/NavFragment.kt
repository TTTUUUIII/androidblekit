package com.outlook.wn123o.androidblekit.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.outlook.wn123o.androidblekit.R
import com.outlook.wn123o.androidblekit.databinding.FragmentNavBinding

class NavFragment : Fragment() {

    private val binding by lazy {
        FragmentNavBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAction()
    }

    private fun setupAction() {
        binding.centralButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_scan)
        }
        binding.peripheralDevice.setOnClickListener {
            findNavController().navigate(R.id.navigation_peripheral)
        }
    }
}