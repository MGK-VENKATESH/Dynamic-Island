package com.example.myapplicationdynamic.dynamicisland

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplicationdynamic.R

class DynamicIslandService : Service() {

    companion object {
        private const val TAG = "DynamicIslandService"
        private const val CHANNEL_ID = "DynamicIslandChannel"
        private const val NOTIFICATION_ID = 1

        // Action constants
        const val ACTION_UPDATE = "com.example.myapplicationdynamic.dynamicisland.UPDATE_ISLAND"
        const val ACTION_SHOW_TIMER = "com.example.myapplicationdynamic.dynamicisland.SHOW_TIMER"
        const val ACTION_SHOW_CALL = "com.example.myapplicationdynamic.dynamicisland.SHOW_CALL"
        const val ACTION_MEDIA = "com.example.myapplicationdynamic.dynamicisland.MEDIA_ACTION"
    }

    private var windowManager: WindowManager? = null
    private var islandView: View? = null
    private var islandContainer: LinearLayout? = null
    private var compactView: LinearLayout? = null
    private var expandedView: LinearLayout? = null

    // Compact views
    private var compactIcon1: ImageView? = null
    private var compactIcon2: ImageView? = null

    // Expanded views
    private var expandedTitle: TextView? = null
    private var expandedSubtitle: TextView? = null
    private var expandedIcon: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var actionButton1: ImageView? = null
    private var actionButton2: ImageView? = null
    private var actionButton3: ImageView? = null

    private var isExpanded = false
    private var currentActivity: IslandActivity = IslandActivity.NONE
    private var expandTimer: CountDownTimer? = null
    private var isViewAdded = false

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received broadcast: ${intent?.action}")
            intent?.let { handleUpdate(it) }
        }
    }

    enum class IslandActivity {
        NONE, MUSIC, TIMER, CALL, CHARGING, FACE_UNLOCK, AIRPODS, RECORDING, SCREEN_RECORDING, ALARM
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate - Starting...")

        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            Log.d(TAG, "Foreground service started")

            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            Log.d(TAG, "WindowManager obtained")

            registerReceivers()
            Log.d(TAG, "Receivers registered")

            // Create island view with delay to ensure everything is ready
            android.os.Handler(mainLooper).post {
                try {
                    createIslandView()
                    Log.d(TAG, "Island view created successfully")

                    // Show test island after 3 seconds
                    android.os.Handler(mainLooper).postDelayed({
                        try {
                            testIsland()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error showing test island", e)
                        }
                    }, 3000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in createIslandView", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dynamic Island Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows Dynamic Island overlay"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island")
            .setContentText("Service is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(ACTION_UPDATE)
            addAction(ACTION_SHOW_TIMER)
            addAction(ACTION_SHOW_CALL)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(updateReceiver, filter)
        }
    }

    private fun createIslandView() {
        try {
            Log.d(TAG, "Inflating island layout...")
            val inflater = LayoutInflater.from(this)
            islandView = inflater.inflate(R.layout.dynamic_island_layout, null)

            if (islandView == null) {
                Log.e(TAG, "Failed to inflate layout!")
                return
            }

            Log.d(TAG, "Finding views...")
            islandContainer = islandView?.findViewById(R.id.islandContainer)
            compactView = islandView?.findViewById(R.id.compactView)
            expandedView = islandView?.findViewById(R.id.expandedView)
            compactIcon1 = islandView?.findViewById(R.id.compactIcon1)
            compactIcon2 = islandView?.findViewById(R.id.compactIcon2)
            expandedTitle = islandView?.findViewById(R.id.expandedTitle)
            expandedSubtitle = islandView?.findViewById(R.id.expandedSubtitle)
            expandedIcon = islandView?.findViewById(R.id.expandedIcon)
            progressBar = islandView?.findViewById(R.id.progressBar)
            actionButton1 = islandView?.findViewById(R.id.actionButton1)
            actionButton2 = islandView?.findViewById(R.id.actionButton2)
            actionButton3 = islandView?.findViewById(R.id.actionButton3)

            if (islandContainer == null) {
                Log.e(TAG, "islandContainer is null!")
                return
            }

            Log.d(TAG, "Creating window layout params...")
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 20

            setupClickListeners()

            Log.d(TAG, "Adding view to window...")
            windowManager?.addView(islandView, params)
            isViewAdded = true
            Log.d(TAG, "Island view added to window successfully")

            // Start in compact mode (hidden)
            showCompactMode()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating island view", e)
            e.printStackTrace()
        }
    }

    private fun testIsland() {
        if (!isViewAdded || islandContainer == null) {
            Log.e(TAG, "Cannot test island - view not ready")
            return
        }

        Log.d(TAG, "Testing island with demo content")
        currentActivity = IslandActivity.MUSIC

        compactIcon1?.setImageResource(android.R.drawable.ic_media_play)
        compactIcon2?.visibility = View.GONE

        expandedTitle?.text = "Test Song"
        expandedSubtitle?.text = "Test Artist"
        expandedIcon?.setImageResource(android.R.drawable.ic_media_play)

        progressBar?.visibility = View.GONE
        actionButton1?.visibility = View.GONE
        actionButton2?.visibility = View.GONE
        actionButton3?.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse(5000)
    }

    private fun setupClickListeners() {
        islandView?.setOnClickListener {
            Log.d(TAG, "Island clicked, current activity: $currentActivity")
            if (currentActivity != IslandActivity.NONE) {
                toggleExpansion()
            }
        }

        islandView?.setOnLongClickListener {
            Log.d(TAG, "Island long pressed")
            openAssociatedApp()
            true
        }

        actionButton1?.setOnClickListener { handleAction1() }
        actionButton2?.setOnClickListener { handleAction2() }
        actionButton3?.setOnClickListener { handleAction3() }
    }

    private fun handleUpdate(intent: Intent) {
        Log.d(TAG, "Handling update: ${intent.action}, type: ${intent.getStringExtra("type")}")

        when (intent.action) {
            ACTION_UPDATE -> {
                val type = intent.getStringExtra("type")
                when (type) {
                    "music" -> handleMusicUpdate(intent)
                    "faceunlock" -> handleFaceUnlock()
                    "airpods" -> handleAirPodsConnection(intent)
                    "recording" -> handleRecording(intent)
                    "hide" -> hideIsland()
                }
            }
            ACTION_SHOW_TIMER -> handleTimer(intent)
            ACTION_SHOW_CALL -> handleCall(intent)
            Intent.ACTION_POWER_CONNECTED -> handleCharging(true)
            Intent.ACTION_POWER_DISCONNECTED -> handleCharging(false)
        }
    }

    private fun handleMusicUpdate(intent: Intent) {
        val isPlaying = intent.getBooleanExtra("isPlaying", false)
        if (!isPlaying) {
            hideIsland()
            return
        }

        currentActivity = IslandActivity.MUSIC
        val title = intent.getStringExtra("title") ?: "Unknown"
        val artist = intent.getStringExtra("artist") ?: "Unknown Artist"

        compactIcon1?.setImageResource(android.R.drawable.ic_media_play)
        compactIcon2?.visibility = View.VISIBLE
        compactIcon2?.setImageResource(android.R.drawable.ic_media_play)

        expandedTitle?.text = title
        expandedSubtitle?.text = artist
        expandedIcon?.setImageResource(android.R.drawable.ic_media_play)

        actionButton1?.setImageResource(android.R.drawable.ic_media_previous)
        actionButton2?.setImageResource(android.R.drawable.ic_media_pause)
        actionButton3?.setImageResource(android.R.drawable.ic_media_next)
        actionButton1?.visibility = View.VISIBLE
        actionButton2?.visibility = View.VISIBLE
        actionButton3?.visibility = View.VISIBLE

        progressBar?.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse()
    }

    private fun handleTimer(intent: Intent) {
        currentActivity = IslandActivity.TIMER
        val duration = intent.getLongExtra("duration", 60000)

        compactIcon1?.setImageResource(android.R.drawable.ic_lock_idle_alarm)
        compactIcon2?.visibility = View.GONE

        expandedTitle?.text = "Timer"
        expandedSubtitle?.text = formatTime(duration)
        expandedIcon?.setImageResource(android.R.drawable.ic_lock_idle_alarm)

        progressBar?.visibility = View.VISIBLE
        progressBar?.max = 100

        actionButton1?.setImageResource(android.R.drawable.ic_media_pause)
        actionButton2?.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        actionButton3?.visibility = View.GONE
        actionButton1?.visibility = View.VISIBLE
        actionButton2?.visibility = View.VISIBLE

        showIslandWithAnimation()
        startTimerCountdown(duration)
    }

    private fun handleCall(intent: Intent) {
        currentActivity = IslandActivity.CALL
        val contactName = intent.getStringExtra("contactName") ?: "Unknown"
        val duration = intent.getStringExtra("duration") ?: "00:00"

        compactIcon1?.setImageResource(android.R.drawable.ic_menu_call)
        compactIcon2?.visibility = View.VISIBLE
        compactIcon2?.setImageResource(android.R.drawable.ic_menu_call)

        expandedTitle?.text = contactName
        expandedSubtitle?.text = duration
        expandedIcon?.setImageResource(android.R.drawable.ic_menu_call)

        actionButton1?.setImageResource(android.R.drawable.ic_btn_speak_now)
        actionButton2?.setImageResource(android.R.drawable.ic_menu_call)
        actionButton2?.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        actionButton3?.visibility = View.GONE
        actionButton1?.visibility = View.VISIBLE
        actionButton2?.visibility = View.VISIBLE

        progressBar?.visibility = View.GONE

        showIslandWithAnimation()
    }

    private fun handleCharging(isCharging: Boolean) {
        if (!isCharging) {
            if (currentActivity == IslandActivity.CHARGING) {
                hideIsland()
            }
            return
        }

        currentActivity = IslandActivity.CHARGING
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        compactIcon1?.setImageResource(android.R.drawable.ic_lock_idle_charging)
        compactIcon2?.visibility = View.VISIBLE
        compactIcon2?.setImageResource(android.R.drawable.ic_lock_idle_charging)

        expandedTitle?.text = "Charging"
        expandedSubtitle?.text = "$batteryLevel%"
        expandedIcon?.setImageResource(android.R.drawable.ic_lock_idle_charging)

        progressBar?.visibility = View.VISIBLE
        progressBar?.progress = batteryLevel

        actionButton1?.visibility = View.GONE
        actionButton2?.visibility = View.GONE
        actionButton3?.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse(3000)
    }

    private fun handleFaceUnlock() {
        currentActivity = IslandActivity.FACE_UNLOCK

        compactIcon1?.setImageResource(android.R.drawable.ic_menu_view)
        compactIcon2?.visibility = View.GONE

        expandedTitle?.text = "Face ID"
        expandedSubtitle?.text = "Scanning..."
        expandedIcon?.setImageResource(android.R.drawable.ic_menu_view)

        progressBar?.visibility = View.VISIBLE
        progressBar?.isIndeterminate = true

        actionButton1?.visibility = View.GONE
        actionButton2?.visibility = View.GONE
        actionButton3?.visibility = View.GONE

        showIslandWithAnimation()

        expandTimer?.cancel()
        expandTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                expandedSubtitle?.text = "Unlocked"
                progressBar?.visibility = View.GONE
                hideIsland(500)
            }
        }.start()
    }

    private fun handleAirPodsConnection(intent: Intent) {
        currentActivity = IslandActivity.AIRPODS
        val isConnected = intent.getBooleanExtra("connected", true)
        val deviceName = intent.getStringExtra("deviceName") ?: "AirPods"

        compactIcon1?.setImageResource(android.R.drawable.ic_btn_speak_now)
        compactIcon2?.visibility = View.GONE

        expandedTitle?.text = deviceName
        expandedSubtitle?.text = if (isConnected) "Connected" else "Disconnected"
        expandedIcon?.setImageResource(android.R.drawable.ic_btn_speak_now)

        progressBar?.visibility = View.GONE
        actionButton1?.visibility = View.GONE
        actionButton2?.visibility = View.GONE
        actionButton3?.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse(2000)
    }

    private fun handleRecording(intent: Intent) {
        val isScreenRecording = intent.getBooleanExtra("isScreenRecording", false)
        currentActivity = if (isScreenRecording) IslandActivity.SCREEN_RECORDING else IslandActivity.RECORDING

        compactIcon1?.setImageResource(android.R.drawable.ic_menu_camera)
        compactIcon2?.visibility = View.VISIBLE
        compactIcon2?.setImageResource(android.R.drawable.presence_video_online)

        expandedTitle?.text = if (isScreenRecording) "Screen Recording" else "Recording"
        expandedSubtitle?.text = "00:00"
        expandedIcon?.setImageResource(android.R.drawable.ic_menu_camera)

        actionButton1?.setImageResource(android.R.drawable.ic_media_pause)
        actionButton2?.setImageResource(android.R.drawable.ic_delete)
        actionButton3?.visibility = View.GONE
        actionButton1?.visibility = View.VISIBLE
        actionButton2?.visibility = View.VISIBLE

        progressBar?.visibility = View.GONE

        showIslandWithAnimation()
    }

    private fun toggleExpansion() {
        if (isExpanded) {
            collapseIsland()
        } else {
            expandIsland()
        }
    }

    private fun expandIsland() {
        Log.d(TAG, "Expanding island")
        isExpanded = true

        compactView?.visibility = View.GONE
        expandedView?.visibility = View.VISIBLE
        expandedView?.alpha = 0f

        islandContainer?.animate()
            ?.scaleX(1.8f)
            ?.scaleY(2.5f)
            ?.setDuration(400)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()

        expandedView?.animate()
            ?.alpha(1f)
            ?.setDuration(300)
            ?.setStartDelay(100)
            ?.start()
    }

    private fun collapseIsland() {
        Log.d(TAG, "Collapsing island")
        isExpanded = false

        expandedView?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                expandedView?.visibility = View.GONE
                compactView?.visibility = View.VISIBLE
            }
            ?.start()

        islandContainer?.animate()
            ?.scaleX(1.0f)
            ?.scaleY(1.0f)
            ?.setDuration(400)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }

    private fun showIslandWithAnimation() {
        Log.d(TAG, "Showing island with animation")
        islandContainer?.scaleY = 0.3f
        islandContainer?.alpha = 0f
        islandContainer?.visibility = View.VISIBLE

        islandContainer?.animate()
            ?.scaleY(1.0f)
            ?.alpha(1f)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }

    private fun hideIsland(delay: Long = 0) {
        Log.d(TAG, "Hiding island with delay: $delay")
        expandTimer?.cancel()

        islandContainer?.postDelayed({
            islandContainer?.animate()
                ?.scaleY(0.3f)
                ?.alpha(0f)
                ?.setDuration(300)
                ?.withEndAction {
                    islandContainer?.visibility = View.GONE
                    currentActivity = IslandActivity.NONE
                    isExpanded = false
                }
                ?.start()
        }, delay)
    }

    private fun autoExpandAndCollapse(displayTime: Long = 3000) {
        expandTimer?.cancel()
        expandTimer = object : CountDownTimer(displayTime, displayTime) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (!isExpanded) {
                    hideIsland()
                }
            }
        }.start()
    }

    private fun showCompactMode() {
        compactView?.visibility = View.VISIBLE
        expandedView?.visibility = View.GONE
        islandContainer?.visibility = View.GONE
    }

    private fun startTimerCountdown(duration: Long) {
        expandTimer?.cancel()
        expandTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                expandedSubtitle?.text = formatTime(millisUntilFinished)
                val progress = ((duration - millisUntilFinished) * 100 / duration).toInt()
                progressBar?.progress = progress
            }
            override fun onFinish() {
                expandedSubtitle?.text = "00:00"
                progressBar?.progress = 100
                hideIsland(1000)
            }
        }.start()
    }

    private fun handleAction1() {
        when (currentActivity) {
            IslandActivity.MUSIC -> sendMediaAction("previous")
            IslandActivity.TIMER -> pauseTimer()
            IslandActivity.CALL -> muteCall()
            else -> {}
        }
    }

    private fun handleAction2() {
        when (currentActivity) {
            IslandActivity.MUSIC -> sendMediaAction("pause")
            IslandActivity.TIMER -> cancelTimer()
            IslandActivity.CALL -> endCall()
            else -> {}
        }
    }

    private fun handleAction3() {
        when (currentActivity) {
            IslandActivity.MUSIC -> sendMediaAction("next")
            else -> {}
        }
    }

    private fun openAssociatedApp() {
        val packageName = when (currentActivity) {
            IslandActivity.MUSIC -> "com.spotify.music"
            IslandActivity.TIMER -> "com.google.android.deskclock"
            IslandActivity.CALL -> "com.android.dialer"
            else -> return
        }

        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app: $packageName", e)
        }
    }

    private fun sendMediaAction(action: String) {
        val intent = Intent(ACTION_MEDIA)
        intent.putExtra("action", action)
        sendBroadcast(intent)
    }

    private fun pauseTimer() {
        expandTimer?.cancel()
    }

    private fun cancelTimer() {
        expandTimer?.cancel()
        hideIsland()
    }

    private fun muteCall() {
        // Implement mute functionality
    }

    private fun endCall() {
        hideIsland()
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        expandTimer?.cancel()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        if (isViewAdded && islandView != null) {
            try {
                windowManager?.removeView(islandView)
                isViewAdded = false
            } catch (e: Exception) {
                Log.e(TAG, "Error removing island view", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}