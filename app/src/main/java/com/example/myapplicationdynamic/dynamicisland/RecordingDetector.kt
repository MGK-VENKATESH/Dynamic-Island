package com.example.myapplicationdynamic.dynamicisland



import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.projection.MediaProjectionManager

class RecordingDetector(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun startVoiceRecording() {
        val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
        intent.putExtra("type", "recording")
        intent.putExtra("isScreenRecording", false)
        context.sendBroadcast(intent)
    }

    fun startScreenRecording() {
        val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
        intent.putExtra("type", "recording")
        intent.putExtra("isScreenRecording", true)
        context.sendBroadcast(intent)
    }

    fun stopRecording() {
        val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
        intent.putExtra("type", "hide")
        context.sendBroadcast(intent)
    }

    fun isRecordingActive(): Boolean {
        // Check if microphone is in use
        return try {
            audioManager.mode == AudioManager.MODE_IN_COMMUNICATION ||
                    audioManager.mode == AudioManager.MODE_IN_CALL
        } catch (e: Exception) {
            false
        }
    }
}