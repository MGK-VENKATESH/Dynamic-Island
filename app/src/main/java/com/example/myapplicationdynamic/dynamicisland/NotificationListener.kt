package com.example.myapplicationdynamic.dynamicisland

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
    }

    private val musicPackages = listOf(
        "com.spotify.music",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.amazon.mp3",
        "com.apple.android.music",
        "com.gaana.gaana",
        "com.jio.media.jiobeats",
        "com.wynk.music",
        "deezer.android.app",
        "com.soundcloud.android"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "✓ Notification Listener Connected!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "✗ Notification Listener Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val packageName = it.packageName
            Log.d(TAG, "📢 Notification from: $packageName")

            // Check if it's a music app
            if (musicPackages.contains(packageName)) {
                val notification = it.notification
                val extras = notification.extras

                val title = extras.getCharSequence("android.title")?.toString() ?: ""
                val text = extras.getCharSequence("android.text")?.toString() ?: ""
                val subText = extras.getCharSequence("android.subText")?.toString() ?: ""

                Log.d(TAG, "🎵 Music notification detected!")
                Log.d(TAG, "   Title: $title")
                Log.d(TAG, "   Text: $text")
                Log.d(TAG, "   SubText: $subText")

                // Only show if we have actual song info
                if (title.isNotEmpty() && title != packageName) {
                    // Send broadcast to update Dynamic Island
                    val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
                    intent.putExtra("type", "music")
                    intent.putExtra("title", title)
                    intent.putExtra("artist", if (text.isNotEmpty()) text else "Playing...")
                    intent.putExtra("isPlaying", true)
                    sendBroadcast(intent)

                    Log.d(TAG, "✓ Broadcast sent to Dynamic Island")
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        sbn?.let {
            if (musicPackages.contains(it.packageName)) {
                Log.d(TAG, "🎵 Music notification removed")

                val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
                intent.putExtra("type", "hide")
                sendBroadcast(intent)

                Log.d(TAG, "✓ Hide broadcast sent")
            }
        }
    }
}