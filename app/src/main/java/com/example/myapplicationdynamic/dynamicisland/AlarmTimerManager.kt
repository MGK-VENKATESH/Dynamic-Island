package com.example.myapplicationdynamic.dynamicisland



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class AlarmTimerManager {

    companion object {
        fun startTimer(context: Context, durationMillis: Long) {
            val intent = Intent("com.yourapp.dynamicisland.SHOW_TIMER")
            intent.putExtra("duration", durationMillis)
            context.sendBroadcast(intent)
        }

        fun startStopwatch(context: Context) {
            val intent = Intent("com.yourapp.dynamicisland.SHOW_TIMER")
            intent.putExtra("duration", Long.MAX_VALUE)
            intent.putExtra("isStopwatch", true)
            context.sendBroadcast(intent)
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val alarmIntent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
            alarmIntent.putExtra("type", "alarm")
            alarmIntent.putExtra("title", "Alarm")
            it.sendBroadcast(alarmIntent)
        }
    }
}