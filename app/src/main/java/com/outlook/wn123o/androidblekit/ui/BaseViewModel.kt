package com.outlook.wn123o.androidblekit.ui

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.outlook.wn123o.androidblekit.common.requireApplicationContext
import com.outlook.wn123o.androidblekit.interfaces.WrapperContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.StringBuilder

open class BaseViewModel: ViewModel(), WrapperContext {
    var txMsg = ""

    private var rxMsg: StringBuilder = StringBuilder("")
    private var _rxMsgState = MutableLiveData<String>("")

    val rxMsgState: LiveData<String> = _rxMsgState

    fun putMsg(msg: String) {
        rxMsg.insert(0, "$msg\n")
        postMsg()
    }

    fun cleanMsg() {
        rxMsg.clear()
        postMsg()
    }

    private fun postMsg() {
        _rxMsgState.postValue(rxMsg.toString())
    }

    private var _remoteAddressState = MutableStateFlow("")
    val remoteAddressState = _remoteAddressState.asStateFlow()

    fun updateRemoteAddressState(newState: String) {
        _remoteAddressState.update {
            newState
        }
    }

    open fun onAction(view: View) {

    }

    fun toast(@StringRes stringRes: Int) {
        Toast.makeText(requireApplicationContext(), stringRes, Toast.LENGTH_SHORT).show()
    }

    fun toast(message: String) {
        Toast.makeText(requireApplicationContext(), message, Toast.LENGTH_SHORT).show()
    }
}