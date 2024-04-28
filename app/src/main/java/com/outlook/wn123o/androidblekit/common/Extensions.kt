package com.outlook.wn123o.androidblekit.common

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment


fun Fragment.toast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(@StringRes id: Int) {
    Toast.makeText(requireContext(), getString(id), Toast.LENGTH_SHORT).show()
}