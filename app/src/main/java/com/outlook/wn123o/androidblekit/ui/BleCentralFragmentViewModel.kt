package com.outlook.wn123o.androidblekit.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BleCentralFragmentViewModel: BaseViewModel() {
    private var _connectState = MutableStateFlow("未连接")
    val connectState = _connectState.asStateFlow()

    fun updateConnectState(newState: String) {
        _connectState.update {
            newState
        }
    }

    private var _remoteRssiState = MutableStateFlow(0)
    val remoteRssiState = _remoteRssiState.asStateFlow()

    fun updateRemoteRssiState(newState: Int) {
        _remoteRssiState.update {
            newState
        }
    }
}