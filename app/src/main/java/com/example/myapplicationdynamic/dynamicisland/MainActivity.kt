package com.example.myapplicationdynamic.dynamicisland

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplicationdynamic.R

class MainActivity : Activity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val OVERLAY_PERMISSION_REQ_CODE = 1234
        private const val PHONE_PERMISSION_REQ_CODE = 1235
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "========== MainActivity started ==========")

        val startButton: Button = findViewById(R.id.btnStart)
        val stopButton: Button = findViewById(R.id.btnStop)

        startButton.setOnClickListener {
            Log.d(TAG, "START button clicked")
            startDynamicIsland()
        }

        stopButton.setOnClickListener {
            Log.d(TAG, "STOP button clicked")
            stopIslandService()
        }

        // Check permissions on startup
        checkAllPermissions()
    }

    private fun checkAllPermissions() {
        val overlayGranted = checkOverlayPermission()
        val phoneGranted = checkPhonePermission()

        Log.d(TAG, "Overlay permission: $overlayGranted")
        Log.d(TAG, "Phone permission: $phoneGranted")
    }

    private fun startDynamicIsland() {
        // Check overlay permission
        if (!checkOverlayPermission()) {
            Log.d(TAG, "Requesting overlay permission...")
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
            return
        }

        // Check phone permission (optional but recommended)
        if (!checkPhonePermission()) {
            Log.d(TAG, "Phone permission not granted, requesting...")
            requestPhonePermission()
        }

        // Start the service
        startIslandService()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun checkPhonePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
    }

    private fun requestPhonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PHONE_PERMISSION_REQ_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                Log.d(TAG, "✓ Overlay permission granted!")
                Toast.makeText(this, "Permission granted! Click START again", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "✗ Overlay permission denied")
                Toast.makeText(
                    this,
                    "Overlay permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PHONE_PERMISSION_REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✓ Phone permission granted")
            } else {
                Log.d(TAG, "✗ Phone permission denied")
            }
        }
    }

    private fun startIslandService() {
        try {
            Log.d(TAG, "Starting Dynamic Island Service...")

            val intent = Intent(this, DynamicIslandService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
                Log.d(TAG, "✓ Foreground service started")
            } else {
                startService(intent)
                Log.d(TAG, "✓ Service started")
            }

            Toast.makeText(
                this,
                "Dynamic Island Started!\nLook at the top of the screen",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Log.e(TAG, "✗ Error starting service", e)
            Toast.makeText(
                this,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun stopIslandService() {
        try {
            val intent = Intent(this, DynamicIslandService::class.java)
            stopService(intent)
            Log.d(TAG, "✓ Service stopped")
            Toast.makeText(this, "Dynamic Island Stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error stopping service", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAllPermissions()
    }
}