package com.outlook.wn123o.androidblekit.common

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.outlook.wn123o.androidblekit.App
import com.outlook.wn123o.androidblekit.interfaces.WrapperContext
import java.io.File
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

fun <T> T.timeZone(format: String = "HH:mm:ss"): String {
    return SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis())
}

fun Fragment.getExtensionName(uri: Uri): String {
    return MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(requireActivity().contentResolver.getType(uri)) ?: "dat"
}

fun WrapperContext.newFileInDownloadsDir(extension: String): File =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
        File(
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/${
                timeZone(
                    "MMddHHmmss"
                )
            }.${extension}"
        )
    } else {
        File(
            "${requireApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath}/${
                timeZone(
                    "HHmmss"
                )
            }.${extension}"
        )
    }

fun <T> Context.getSetting(key: String, defaultValue: T): T {
    return (PreferenceManager.getDefaultSharedPreferences(this).all[key] ?: defaultValue) as T
}

fun <T> Context.getSetting(keyRes: Int, defaultValue: T): T {
    return getSetting(getString(keyRes), defaultValue)
}