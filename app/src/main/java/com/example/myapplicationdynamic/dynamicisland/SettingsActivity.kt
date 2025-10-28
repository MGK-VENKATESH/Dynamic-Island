package com.yourapp.dynamicisland

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var positionSeekBar: SeekBar
    private lateinit var sizeSeekBar: SeekBar
    private lateinit var autoExpandSwitch: Switch
    private lateinit var vibrateSwitch: Switch
    private lateinit var showMusicSwitch: Switch
    private lateinit var showCallsSwitch: Switch
    private lateinit var showChargingSwitch: Switch
    private lateinit var showTimerSwitch: Switch
    private lateinit var showBluetoothSwitch: Switch
    private lateinit var showFaceUnlockSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("DynamicIslandPrefs", MODE_PRIVATE)

        initializeViews()
        loadSettings()
        setupListeners()
    }

    private fun initializeViews() {
        positionSeekBar = findViewById(R.id.positionSeekBar)
        sizeSeekBar = findViewById(R.id.sizeSeekBar)
        autoExpandSwitch = findViewById(R.id.autoExpandSwitch)
        vibrateSwitch = findViewById(R.id.vibrateSwitch)
        showMusicSwitch = findViewById(R.id.showMusicSwitch)
        showCallsSwitch = findViewById(R.id.showCallsSwitch)
        showChargingSwitch = findViewById(R.id.showChargingSwitch)
        showTimerSwitch = findViewById(R.id.showTimerSwitch)
        showBluetoothSwitch = findViewById(R.id.showBluetoothSwitch)
        showFaceUnlockSwitch = findViewById(R.id.showFaceUnlockSwitch)

        findViewById<Button>(R.id.btnSave).setOnClickListener { saveSettings() }
        findViewById<Button>(R.id.btnReset).setOnClickListener { resetToDefaults() }
    }

    private fun loadSettings() {
        positionSeekBar.progress = prefs.getInt("island_position", 20)
        sizeSeekBar.progress = prefs.getInt("island_size", 50)
        autoExpandSwitch.isChecked = prefs.getBoolean("auto_expand", true)
        vibrateSwitch.isChecked = prefs.getBoolean("vibrate", true)
        showMusicSwitch.isChecked = prefs.getBoolean("show_music", true)
        showCallsSwitch.isChecked = prefs.getBoolean("show_calls", true)
        showChargingSwitch.isChecked = prefs.getBoolean("show_charging", true)
        showTimerSwitch.isChecked = prefs.getBoolean("show_timer", true)
        showBluetoothSwitch.isChecked = prefs.getBoolean("show_bluetooth", true)
        showFaceUnlockSwitch.isChecked = prefs.getBoolean("show_face_unlock", true)
    }

    private fun setupListeners() {
        val positionText: TextView = findViewById(R.id.positionValue)
        val sizeText: TextView = findViewById(R.id.sizeValue)

        positionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                positionText.text = "${progress}dp"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sizeText.text = "${progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveSettings() {
        prefs.edit().apply {
            putInt("island_position", positionSeekBar.progress)
            putInt("island_size", sizeSeekBar.progress)
            putBoolean("auto_expand", autoExpandSwitch.isChecked)
            putBoolean("vibrate", vibrateSwitch.isChecked)
            putBoolean("show_music", showMusicSwitch.isChecked)
            putBoolean("show_calls", showCallsSwitch.isChecked)
            putBoolean("show_charging", showChargingSwitch.isChecked)
            putBoolean("show_timer", showTimerSwitch.isChecked)
            putBoolean("show_bluetooth", showBluetoothSwitch.isChecked)
            putBoolean("show_face_unlock", showFaceUnlockSwitch.isChecked)
            apply()
        }

        // Notify service to update settings
        val intent = Intent("com.yourapp.dynamicisland.SETTINGS_CHANGED")
        sendBroadcast(intent)

        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetToDefaults() {
        prefs.edit().clear().apply()
        loadSettings()
        Toast.makeText(this, getString(R.string.reset_to_defaults), Toast.LENGTH_SHORT).show()
    }
}