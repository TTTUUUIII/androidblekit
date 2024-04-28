package com.outlook.wn123o.androidblekit.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.StringBuilder

open class BaseViewModel: ViewModel() {
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
}