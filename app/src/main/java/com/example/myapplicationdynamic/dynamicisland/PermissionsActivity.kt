package com.example.myapplicationdynamic.dynamicisland



import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1001
    private val NOTIFICATION_PERMISSION_CODE = 1002
    private val PHONE_PERMISSION_CODE = 1003
    private val BLUETOOTH_PERMISSION_CODE = 1004

    private val requiredPermissions = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        setupPermissionsList()
        setupButtons()
    }

    private fun setupPermissionsList() {
        val container: LinearLayout = findViewById(R.id.permissionsContainer)

        // Overlay Permission
        addPermissionItem(container, "Draw Over Other Apps",
            "Required to display Dynamic Island",
            Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        // Notification Access
        addPermissionItem(container, "Notification Access",
            "Required to detect music playback",
            isNotificationAccessGranted()) {
            requestNotificationAccess()
        }

        // Phone Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPermissionItem(container, "Phone State",
                "Required to show call information",
                checkPermission(Manifest.permission.READ_PHONE_STATE)) {
                requestPhonePermission()
            }
        }

        // Bluetooth Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addPermissionItem(container, "Bluetooth",
                "Required to detect device connections",
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                requestBluetoothPermission()
            }
        }
    }

    private fun addPermissionItem(container: LinearLayout, title: String,
                                  description: String, isGranted: Boolean,
                                  onClick: () -> Unit) {
        val itemView = layoutInflater.inflate(R.layout.permission_item, container, false)
        itemView.findViewById<TextView>(R.id.permissionTitle).text = title
        itemView.findViewById<TextView>(R.id.permissionDescription).text = description

        val statusText = itemView.findViewById<TextView>(R.id.permissionStatus)
        val grantButton = itemView.findViewById<Button>(R.id.grantButton)

        if (isGranted) {
            statusText.text = "✓ Granted"
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            grantButton.isEnabled = false
            grantButton.text = "Granted"
        } else {
            statusText.text = "✗ Not Granted"
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
                android.widget.Toast.makeText(this,
                    "Please grant all required permissions",
                    android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnGrantAll).setOnClickListener {
            requestAllPermissions()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"))
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }

    private fun requestNotificationAccess() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun requestPhonePermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            PHONE_PERMISSION_CODE)
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                BLUETOOTH_PERMISSION_CODE)
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
            contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    private fun checkAllPermissions(): Boolean {
        return Settings.canDrawOverlays(this) &&
                isNotificationAccessGranted() &&
                checkPermission(Manifest.permission.READ_PHONE_STATE)
    }

    override fun onResume() {
        super.onResume()
        // Refresh permissions status
        findViewById<LinearLayout>(R.id.permissionsContainer).removeAllViews()
        setupPermissionsList()
    }
}