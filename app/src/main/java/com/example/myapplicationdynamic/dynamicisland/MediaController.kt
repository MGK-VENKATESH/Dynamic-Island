package com.example.myapplicationdynamic.dynamicisland



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent

class MediaController : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.yourapp.dynamicisland.MEDIA_ACTION") {
            val action = intent.getStringExtra("action")
            context?.let {
                when (action) {
                    "play" -> sendMediaButton(it, KeyEvent.KEYCODE_MEDIA_PLAY)
                    "pause" -> sendMediaButton(it, KeyEvent.KEYCODE_MEDIA_PAUSE)
                    "next" -> sendMediaButton(it, KeyEvent.KEYCODE_MEDIA_NEXT)
                    "previous" -> sendMediaButton(it, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                }
            }
        }
    }

    private fun sendMediaButton(context: Context, keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }
}