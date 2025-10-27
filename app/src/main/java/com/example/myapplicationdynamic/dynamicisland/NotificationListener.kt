package com.example.myapplicationdynamic.dynamicisland



import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    private val musicPackages = listOf(
        "com.spotify.music",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.amazon.mp3",
        "com.apple.android.music"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val packageName = it.packageName

            // Check if it's a music app
            if (musicPackages.contains(packageName)) {
                val notification = it.notification
                val extras = notification.extras

                val title = extras.getString("android.title", "")
                val text = extras.getString("android.text", "")

                // Send broadcast to update Dynamic Island
                val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
                intent.putExtra("type", "music")
                intent.putExtra("title", title)
                intent.putExtra("artist", text)
                intent.putExtra("isPlaying", true)
                sendBroadcast(intent)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        sbn?.let {
            if (musicPackages.contains(it.packageName)) {
                val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
                intent.putExtra("type", "music")
                intent.putExtra("isPlaying", false)
                sendBroadcast(intent)
            }
        }
    }
}