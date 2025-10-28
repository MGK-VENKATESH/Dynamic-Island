package com.example.myapplicationdynamic.dynamicisland

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var callTimer: CountDownTimer? = null
        private var callStartTime: Long = 0
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            context?.let {
                val contactName = if (phoneNumber != null) {
                    ContactHelper.getContactName(it, phoneNumber)
                } else {
                    "Unknown"
                }

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        showCallIsland(it, "Incoming Call", contactName, true)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        callStartTime = System.currentTimeMillis()
                        startCallTimer(it, contactName)
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        stopCallTimer()
                        hideCallIsland(it)
                    }
                }
            }
        }
    }

    private fun showCallIsland(context: Context, title: String, contact: String, isIncoming: Boolean) {
        val intent = Intent("com.yourapp.dynamicisland.SHOW_CALL")
        intent.putExtra("contactName", contact)
        intent.putExtra("duration", if (isIncoming) "Incoming..." else "00:00")
        intent.putExtra("isIncoming", isIncoming)
        context.sendBroadcast(intent)
    }

    private fun startCallTimer(context: Context, contact: String) {
        callTimer?.cancel()
        callTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val duration = System.currentTimeMillis() - callStartTime
                val seconds = (duration / 1000) % 60
                val minutes = (duration / 1000) / 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)

                val intent = Intent("com.yourapp.dynamicisland.SHOW_CALL")
                intent.putExtra("contactName", contact)
                intent.putExtra("duration", formattedTime)
                intent.putExtra("isIncoming", false)
                context.sendBroadcast(intent)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun stopCallTimer() {
        callTimer?.cancel()
        callTimer = null
    }

    private fun hideCallIsland(context: Context) {
        val intent = Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
        intent.putExtra("type", "hide")
        context.sendBroadcast(intent)
    }
}