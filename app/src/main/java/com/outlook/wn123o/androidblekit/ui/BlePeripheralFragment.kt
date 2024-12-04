package com.outlook.wn123o.androidblekit.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.outlook.wn123o.androidblekit.common.Msg
import com.outlook.wn123o.androidblekit.common.MsgHistoryViewAdapter
import com.outlook.wn123o.androidblekit.common.getExtensionName
import com.outlook.wn123o.androidblekit.databinding.FragmentBlePeripheralBinding
import com.outlook.wn123o.androidblekit.databinding.MessageWindowViewBinding

class BlePeripheralFragment : Fragment() {

    private lateinit var startActivityForResult: ActivityResultLauncher<Intent>
    private var req = -1
    private val binding by lazy {
        FragmentBlePeripheralBinding.inflate(layoutInflater)
    }

    private val messageBinding: MessageWindowViewBinding by lazy {
        MessageWindowViewBinding.inflate(layoutInflater, binding.messageWindow, true)
    }

    private val mViewModel by lazy {
        ViewModelProvider(this)[BlePeripheralFragmentViewModel::class.java]
    }

    private val adapter = MsgHistoryViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            if (event == BlePeripheralFragmentViewModel.EVENT_SELECT_SEND_FILE) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .apply {
                        type = "*/*"
                    }
                startActivityForResult.launch(intent)
                req = REQ_SELECT_FILE
            }
        }
        mViewModel.setBlePeripheralEnable(true)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.setBlePeripheralEnable(false)
    }

    companion object {
        const val REQ_SELECT_FILE = BlePeripheralFragmentViewModel.EVENT_SELECT_SEND_FILE
    }
}