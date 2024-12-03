package com.outlook.wn123o.androidblekit

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when(item.itemId) {
            R.id.settings -> {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_settings_fragment)
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_CODE_PERMISSION, REQ_CODE_ACCESS_ALL_FILES -> {
                checkPermissions()
            }
            else -> {}
        }
    }

    private var dialogShowing = false
    private fun checkPermissions() {
        val needRequest = mutableListOf<String>()
        for (permission in mRuntimePermission) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                needRequest.add(permission)
            }
        }
        if (needRequest.isNotEmpty()) {
            requestPermissions(needRequest.toTypedArray(), REQ_CODE_PERMISSION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager() && !dialogShowing) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notice)
                .setIcon(R.mipmap.ic_launcher_round)
                .setMessage(R.string.need_write_store_premission)
                .setPositiveButton(R.string.open) {_, _ ->
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), REQ_CODE_ACCESS_ALL_FILES)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
            dialogShowing = true
        }
    }

    private companion object {
        const val REQ_CODE_PERMISSION = 0
        const val REQ_CODE_ACCESS_ALL_FILES = 1
    }
}