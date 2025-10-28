package com.yourapp.dynamicisland

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 1001
    private val PHONE_PERMISSION_CODE = 1003
    private val BLUETOOTH_PERMISSION_CODE = 1004

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        setupPermissionsList()
        setupButtons()
    }

    private fun setupPermissionsList() {
        val container: LinearLayout = findViewById(R.id.permissionsContainer)
        container.removeAllViews() // Clear previous items

        // Overlay Permission
        addPermissionItem(
            container, getString(R.string.draw_over_apps),
            getString(R.string.draw_over_apps_desc),
            Settings.canDrawOverlays(this)
        ) {
            requestOverlayPermission()
        }

        // Notification Access
        addPermissionItem(
            container, getString(R.string.notification_access),
            getString(R.string.notification_access_desc),
            isNotificationAccessGranted()
        ) {
            requestNotificationAccess()
        }

        // Phone Permission
        addPermissionItem(
            container, getString(R.string.phone_state),
            getString(R.string.phone_state_desc),
            checkPermission(Manifest.permission.READ_PHONE_STATE)
        ) {
            requestPhonePermission()
        }

        // Bluetooth Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addPermissionItem(
                container, getString(R.string.bluetooth),
                getString(R.string.bluetooth_desc),
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT)
            ) {
                requestBluetoothPermission()
            }
        }
    }

    private fun addPermissionItem(
        container: LinearLayout, title: String,
        description: String, isGranted: Boolean,
        onClick: () -> Unit
    ) {
        val itemView = layoutInflater.inflate(R.layout.permission_item, container, false)
        itemView.findViewById<TextView>(R.id.permissionTitle).text = title
        itemView.findViewById<TextView>(R.id.permissionDescription).text = description

        val statusText = itemView.findViewById<TextView>(R.id.permissionStatus)
        val grantButton = itemView.findViewById<Button>(R.id.grantButton)

        if (isGranted) {
            statusText.text = getString(R.string.granted)
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            grantButton.isEnabled = false
            grantButton.text = getString(R.string.granted)
        } else {
            statusText.text = getString(R.string.not_granted)
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            grantButton.setOnClickListener { onClick() }
        }

        container.addView(itemView)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            if (checkAllPermissions()) {
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.grant_all_permissions),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.btnGrantAll).setOnClickListener {
            requestAllPermissions()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
    }

    private fun requestNotificationAccess() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun requestPhonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            PHONE_PERMISSION_CODE
        )
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                BLUETOOTH_PERMISSION_CODE
            )
        }
    }

    private fun requestAllPermissions() {
        if (!Settings.canDrawOverlays(this)) requestOverlayPermission()
        if (!isNotificationAccessGranted()) requestNotificationAccess()
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) requestPhonePermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) requestBluetoothPermission()
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }

    private fun checkAllPermissions(): Boolean {
        var allGranted = Settings.canDrawOverlays(this) &&
                isNotificationAccessGranted() &&
                checkPermission(Manifest.permission.READ_PHONE_STATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            allGranted = allGranted && checkPermission(Manifest.permission.BLUETOOTH_CONNECT)
        }

        return allGranted
    }

    override fun onResume() {
        super.onResume()
        // Refresh permissions status
        setupPermissionsList()
    }
}