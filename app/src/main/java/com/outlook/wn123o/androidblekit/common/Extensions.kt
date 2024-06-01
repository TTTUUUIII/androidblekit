package com.outlook.wn123o.androidblekit.common

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.outlook.wn123o.androidblekit.App
import com.outlook.wn123o.androidblekit.interfaces.WrapperContext
import java.text.SimpleDateFormat
import java.util.Locale


fun Fragment.toast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(@StringRes id: Int) {
    toast(getString(id))
}

fun <T> T.error(tag: String, msg: String) = Log.e(tag, msg)
fun <T> T.warn(tag: String, msg: String) = Log.w(tag, msg)

fun WrapperContext.applicationContext(): Context? = App.getGlobalContext()

fun WrapperContext.getString(@StringRes resId: Int) = requireApplicationContext().getString(resId)
fun WrapperContext.getColor(@ColorRes resId: Int) = requireApplicationContext().getColor(resId)

fun WrapperContext.requireApplicationContext(): Context = applicationContext()!!

val simpleDateFormat = SimpleDateFormat( "HH:mm:ss", Locale.US)
fun <T> T.timeZone(): String = simpleDateFormat.format(System.currentTimeMillis())