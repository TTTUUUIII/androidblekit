package com.outlook.wn123o.androidblekit

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private val mViewModel by lazy {
        ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    private val mRuntimePermission: MutableList<String> = mutableListOf()

     init{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mRuntimePermission.add(android.Manifest.permission.BLUETOOTH_CONNECT);
            mRuntimePermission.add(android.Manifest.permission.BLUETOOTH_SCAN);
            mRuntimePermission.add(android.Manifest.permission.BLUETOOTH_ADVERTISE);
            mRuntimePermission.add(android.Manifest.permission.BLUETOOTH_CONNECT);
        }
         mRuntimePermission.add(android.Manifest.permission.BLUETOOTH);
         mRuntimePermission.add(android.Manifest.permission.BLUETOOTH_ADMIN);
         mRuntimePermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
         mRuntimePermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_CODE_PERMISSION -> {
                checkPermissions()
            }
            else -> {}
        }
    }

    private fun checkPermissions() {
        val needRequest = mutableListOf<String>()
        for (permission in mRuntimePermission) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                needRequest.add(permission)
            }
        }
        if (!needRequest.isEmpty()) {
            requestPermissions(needRequest.toTypedArray(), REQ_CODE_PERMISSION)
        }
    }

    private companion object {
        const val REQ_CODE_PERMISSION = 0
    }
}