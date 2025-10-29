package com.example.myapplicationdynamic.dynamicisland

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.myapplicationdynamic.R

class MainActivity : Activity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val OVERLAY_PERMISSION_REQ_CODE = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity created")

        val startButton: Button = findViewById(R.id.btnStart)
        val stopButton: Button = findViewById(R.id.btnStop)

        startButton.setOnClickListener {
            Log.d(TAG, "Start button clicked")
            if (checkOverlayPermission()) {
                startIslandService()
            } else {
                Log.d(TAG, "Overlay permission not granted, requesting...")
                requestOverlayPermission()
            }
        }

        stopButton.setOnClickListener {
            Log.d(TAG, "Stop button clicked")
            stopIslandService()
        }

        // Check permission status on startup
        if (checkOverlayPermission()) {
            Log.d(TAG, "Overlay permission already granted")
        } else {
            Log.d(TAG, "Overlay permission not granted")
        }
    }

    private fun checkOverlayPermission(): Boolean {
        val canDraw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Permission not needed for older versions
        }
        Log.d(TAG, "Can draw overlays: $canDraw")
        return canDraw
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                Log.d(TAG, "Permission granted, starting service")
                startIslandService()
            } else {
                Log.d(TAG, "Permission denied")
                Toast.makeText(
                    this,
                    "Overlay permission is required for Dynamic Island",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startIslandService() {
        try {
            val intent = Intent(this, DynamicIslandService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
                Log.d(TAG, "Started foreground service")
            } else {
                startService(intent)
                Log.d(TAG, "Started service")
            }

            Toast.makeText(this, "Dynamic Island Started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
            Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopIslandService() {
        try {
            val intent = Intent(this, DynamicIslandService::class.java)
            stopService(intent)
            Log.d(TAG, "Service stopped")
            Toast.makeText(this, "Dynamic Island Stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service", e)
            Toast.makeText(this, "Error stopping service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - checking permission status")
        // Recheck permission when returning from settings
        if (checkOverlayPermission()) {
            Log.d(TAG, "Permission is granted")
        }
    }
}