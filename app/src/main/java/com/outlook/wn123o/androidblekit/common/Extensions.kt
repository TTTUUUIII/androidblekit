package com.outlook.wn123o.androidblekit.common

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment


fun Fragment.toast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(@StringRes id: Int) {
    toast(getString(id))
}

fun Fragment.runOnUiThread(action: Runnable) {
    requireActivity()
        .runOnUiThread(action)
}