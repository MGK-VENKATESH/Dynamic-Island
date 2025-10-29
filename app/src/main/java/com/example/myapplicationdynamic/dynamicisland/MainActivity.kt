package com.example.myapplicationdynamic.dynamicisland

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import com.example.myapplicationdynamic.R

class MainActivity : Activity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton: Button = findViewById(R.id.btnStart)
        val stopButton: Button = findViewById(R.id.btnStop)

        startButton.setOnClickListener {
            if (checkOverlayPermission()) {
                startIslandService()
            } else {
                requestOverlayPermission()
            }
        }

        stopButton.setOnClickListener {
            stopIslandService()
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (checkOverlayPermission()) {
                startIslandService()
            } else {
                Toast.makeText(
                    this,
                    "Overlay permission is required for Dynamic Island",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startIslandService() {
        val intent = Intent(this, DynamicIslandService::class.java)
        startService(intent)
        Toast.makeText(this, "Dynamic Island Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopIslandService() {
        val intent = Intent(this, DynamicIslandService::class.java)
        stopService(intent)
        Toast.makeText(this, "Dynamic Island Stopped", Toast.LENGTH_SHORT).show()
    }
}