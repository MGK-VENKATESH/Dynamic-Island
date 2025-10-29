package com.example.myapplicationdynamic.dynamicisland

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.myapplicationdynamic.R

class SettingsActivity : Activity() {

    private lateinit var prefs: android.content.SharedPreferences

    // UI Elements
    private lateinit var positionSeekBar: SeekBar
    private lateinit var positionValue: TextView
    private lateinit var sizeSeekBar: SeekBar
    private lateinit var sizeValue: TextView
    private lateinit var autoExpandSwitch: Switch
    private lateinit var vibrateSwitch: Switch
    private lateinit var showMusicSwitch: Switch
    private lateinit var showCallsSwitch: Switch
    private lateinit var showChargingSwitch: Switch
    private lateinit var showTimerSwitch: Switch
    private lateinit var showBluetoothSwitch: Switch
    private lateinit var showFaceUnlockSwitch: Switch
    private lateinit var btnReset: Button
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("DynamicIslandSettings", Context.MODE_PRIVATE)

        initializeViews()
        loadSettings()
        setupListeners()
    }

    private fun initializeViews() {
        positionSeekBar = findViewById(R.id.positionSeekBar)
        positionValue = findViewById(R.id.positionValue)
        sizeSeekBar = findViewById(R.id.sizeSeekBar)
        sizeValue = findViewById(R.id.sizeValue)
        autoExpandSwitch = findViewById(R.id.autoExpandSwitch)
        vibrateSwitch = findViewById(R.id.vibrateSwitch)
        showMusicSwitch = findViewById(R.id.showMusicSwitch)
        showCallsSwitch = findViewById(R.id.showCallsSwitch)
        showChargingSwitch = findViewById(R.id.showChargingSwitch)
        showTimerSwitch = findViewById(R.id.showTimerSwitch)
        showBluetoothSwitch = findViewById(R.id.showBluetoothSwitch)
        showFaceUnlockSwitch = findViewById(R.id.showFaceUnlockSwitch)
        btnReset = findViewById(R.id.btnReset)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun loadSettings() {
        // Load saved values or use defaults
        val position = prefs.getInt("position", 20)
        val size = prefs.getInt("size", 50)
        val autoExpand = prefs.getBoolean("autoExpand", true)
        val vibrate = prefs.getBoolean("vibrate", true)
        val showMusic = prefs.getBoolean("showMusic", true)
        val showCalls = prefs.getBoolean("showCalls", true)
        val showCharging = prefs.getBoolean("showCharging", true)
        val showTimer = prefs.getBoolean("showTimer", true)
        val showBluetooth = prefs.getBoolean("showBluetooth", true)
        val showFaceUnlock = prefs.getBoolean("showFaceUnlock", true)

        // Set values
        positionSeekBar.progress = position
        positionValue.text = "${position}dp"

        sizeSeekBar.progress = size
        sizeValue.text = "${size}%"

        autoExpandSwitch.isChecked = autoExpand
        vibrateSwitch.isChecked = vibrate
        showMusicSwitch.isChecked = showMusic
        showCallsSwitch.isChecked = showCalls
        showChargingSwitch.isChecked = showCharging
        showTimerSwitch.isChecked = showTimer
        showBluetoothSwitch.isChecked = showBluetooth
        showFaceUnlockSwitch.isChecked = showFaceUnlock
    }

    private fun setupListeners() {
        // Position SeekBar
        positionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                positionValue.text = "${progress}dp"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Size SeekBar
        sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sizeValue.text = "${progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save Button
        btnSave.setOnClickListener {
            saveSettings()
        }

        // Reset Button
        btnReset.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun saveSettings() {
        val editor = prefs.edit()

        editor.putInt("position", positionSeekBar.progress)
        editor.putInt("size", sizeSeekBar.progress)
        editor.putBoolean("autoExpand", autoExpandSwitch.isChecked)
        editor.putBoolean("vibrate", vibrateSwitch.isChecked)
        editor.putBoolean("showMusic", showMusicSwitch.isChecked)
        editor.putBoolean("showCalls", showCallsSwitch.isChecked)
        editor.putBoolean("showCharging", showChargingSwitch.isChecked)
        editor.putBoolean("showTimer", showTimerSwitch.isChecked)
        editor.putBoolean("showBluetooth", showBluetoothSwitch.isChecked)
        editor.putBoolean("showFaceUnlock", showFaceUnlockSwitch.isChecked)

        editor.apply()

        Toast.makeText(this, "Settings saved! ✓", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetToDefaults() {
        positionSeekBar.progress = 20
        sizeSeekBar.progress = 50
        autoExpandSwitch.isChecked = true
        vibrateSwitch.isChecked = true
        showMusicSwitch.isChecked = true
        showCallsSwitch.isChecked = true
        showChargingSwitch.isChecked = true
        showTimerSwitch.isChecked = true
        showBluetoothSwitch.isChecked = true
        showFaceUnlockSwitch.isChecked = true

        Toast.makeText(this, "Reset to defaults", Toast.LENGTH_SHORT).show()
    }
}