package com.example.myapplicationdynamic.dynamicisland



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                // Screen turned on - show face unlock animation
                context?.let {
                    val unlockIntent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
                    unlockIntent.putExtra("type", "faceunlock")
                    it.sendBroadcast(unlockIntent)
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                // Device unlocked successfully
            }
        }
    }
}