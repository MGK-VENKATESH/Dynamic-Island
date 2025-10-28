package com.example.myapplicationdynamic.dynamicisland

import android.app.Service
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat

class DynamicIslandService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var islandView: View
    private lateinit var islandContainer: LinearLayout
    private lateinit var compactView: LinearLayout
    private lateinit var expandedView: LinearLayout

    // Compact views
    private lateinit var compactIcon1: ImageView
    private lateinit var compactIcon2: ImageView

    // Expanded views
    private lateinit var expandedTitle: TextView
    private lateinit var expandedSubtitle: TextView
    private lateinit var expandedIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var actionButton1: ImageView
    private lateinit var actionButton2: ImageView
    private lateinit var actionButton3: ImageView

    private var isExpanded = false
    private var currentActivity: IslandActivity = IslandActivity.NONE
    private var expandTimer: CountDownTimer? = null

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { handleUpdate(it) }
        }
    }

    enum class IslandActivity {
        NONE, MUSIC, TIMER, CALL, CHARGING, FACE_UNLOCK, AIRPODS, RECORDING, SCREEN_RECORDING, ALARM
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createIslandView()
        registerReceivers()
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction("com.yourapp.dynamicisland.UPDATE_ISLAND")
            addAction("com.yourapp.dynamicisland.SHOW_TIMER")
            addAction("com.yourapp.dynamicisland.SHOW_CALL")
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(updateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    private fun createIslandView() {
        // Create views programmatically
        islandView = createDynamicIslandLayout()

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
        windowManager.addView(islandView, params)

        // Start in compact mode
        showCompactMode()
    }

    private fun createDynamicIslandLayout(): View {
        // Main container
        islandContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.BLACK)
                cornerRadius = 60f
            }
            background = drawable
            setPadding(32, 24, 32, 24)
            elevation = 10f
        }

        // Compact view
        compactView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        compactIcon1 = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(72, 72)
            setColorFilter(Color.WHITE)
        }

        compactIcon2 = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(72, 72).apply {
                marginStart = 24
            }
            setColorFilter(Color.WHITE)
            visibility = View.GONE
        }

        compactView.addView(compactIcon1)
        compactView.addView(compactIcon2)

        // Expanded view
        expandedView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(32, 16, 32, 16)
        }

        expandedIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(96, 96)
            setColorFilter(Color.WHITE)
        }

        expandedTitle = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }

        expandedSubtitle = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
        }

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(400, 20).apply {
                topMargin = 16
            }
            visibility = View.GONE
        }

        val actionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
        }

        actionButton1 = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                marginEnd = 24
            }
            setColorFilter(Color.WHITE)
            visibility = View.GONE
        }

        actionButton2 = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                marginEnd = 24
            }
            setColorFilter(Color.WHITE)
            visibility = View.GONE
        }

        actionButton3 = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80)
            setColorFilter(Color.WHITE)
            visibility = View.GONE
        }

        actionLayout.addView(actionButton1)
        actionLayout.addView(actionButton2)
        actionLayout.addView(actionButton3)

        expandedView.addView(expandedIcon)
        expandedView.addView(expandedTitle)
        expandedView.addView(expandedSubtitle)
        expandedView.addView(progressBar)
        expandedView.addView(actionLayout)

        islandContainer.addView(compactView)
        islandContainer.addView(expandedView)

        return islandContainer
    }

    private fun setupClickListeners() {
        islandView.setOnClickListener {
            if (currentActivity != IslandActivity.NONE) {
                toggleExpansion()
            }
        }

        islandView.setOnLongClickListener {
            openAssociatedApp()
            true
        }

        actionButton1.setOnClickListener { handleAction1() }
        actionButton2.setOnClickListener { handleAction2() }
        actionButton3.setOnClickListener { handleAction3() }
    }

    private fun handleUpdate(intent: Intent) {
        when (intent.action) {
            "com.yourapp.dynamicisland.UPDATE_ISLAND" -> {
                val type = intent.getStringExtra("type")
                when (type) {
                    "music" -> handleMusicUpdate(intent)
                    "faceunlock" -> handleFaceUnlock()
                    "airpods" -> handleAirPodsConnection(intent)
                    "recording" -> handleRecording(intent)
                }
            }
            "com.yourapp.dynamicisland.SHOW_TIMER" -> {
                handleTimer(intent)
            }
            "com.yourapp.dynamicisland.SHOW_CALL" -> {
                handleCall(intent)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                handleCharging(true)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                handleCharging(false)
            }
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

        compactIcon1.setImageResource(android.R.drawable.ic_media_play)
        compactIcon2.visibility = View.VISIBLE
        compactIcon2.setImageResource(android.R.drawable.ic_media_play)

        expandedTitle.text = title
        expandedSubtitle.text = artist
        expandedIcon.setImageResource(android.R.drawable.ic_media_play)

        actionButton1.setImageResource(android.R.drawable.ic_media_previous)
        actionButton2.setImageResource(android.R.drawable.ic_media_pause)
        actionButton3.setImageResource(android.R.drawable.ic_media_next)
        actionButton1.visibility = View.VISIBLE
        actionButton2.visibility = View.VISIBLE
        actionButton3.visibility = View.VISIBLE

        progressBar.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse()
    }

    private fun handleTimer(intent: Intent) {
        currentActivity = IslandActivity.TIMER
        val duration = intent.getLongExtra("duration", 60000)

        compactIcon1.setImageResource(android.R.drawable.ic_lock_idle_alarm)
        compactIcon2.visibility = View.GONE

        expandedTitle.text = "Timer"
        expandedSubtitle.text = formatTime(duration)
        expandedIcon.setImageResource(android.R.drawable.ic_lock_idle_alarm)

        progressBar.visibility = View.VISIBLE
        progressBar.max = 100

        actionButton1.setImageResource(android.R.drawable.ic_media_pause)
        actionButton2.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        actionButton3.visibility = View.GONE
        actionButton1.visibility = View.VISIBLE
        actionButton2.visibility = View.VISIBLE

        showIslandWithAnimation()
        startTimerCountdown(duration)
    }

    private fun handleCall(intent: Intent) {
        currentActivity = IslandActivity.CALL
        val contactName = intent.getStringExtra("contactName") ?: "Unknown"
        val duration = intent.getStringExtra("duration") ?: "00:00"

        compactIcon1.setImageResource(android.R.drawable.ic_menu_call)
        compactIcon2.visibility = View.VISIBLE
        compactIcon2.setImageResource(android.R.drawable.ic_menu_call)

        expandedTitle.text = contactName
        expandedSubtitle.text = duration
        expandedIcon.setImageResource(android.R.drawable.ic_menu_call)

        actionButton1.setImageResource(android.R.drawable.ic_btn_speak_now)
        actionButton2.setImageResource(android.R.drawable.ic_menu_call)
        actionButton2.setColorFilter(Color.RED)
        actionButton3.visibility = View.GONE
        actionButton1.visibility = View.VISIBLE
        actionButton2.visibility = View.VISIBLE

        progressBar.visibility = View.GONE

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

        compactIcon1.setImageResource(android.R.drawable.ic_lock_idle_charging)
        compactIcon2.visibility = View.VISIBLE
        compactIcon2.setImageResource(android.R.drawable.ic_lock_idle_charging)

        expandedTitle.text = "Charging"
        expandedSubtitle.text = "$batteryLevel%"
        expandedIcon.setImageResource(android.R.drawable.ic_lock_idle_charging)

        progressBar.visibility = View.VISIBLE
        progressBar.progress = batteryLevel

        actionButton1.visibility = View.GONE
        actionButton2.visibility = View.GONE
        actionButton3.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse(3000)
    }

    private fun handleFaceUnlock() {
        currentActivity = IslandActivity.FACE_UNLOCK

        compactIcon1.setImageResource(android.R.drawable.ic_menu_view)
        compactIcon2.visibility = View.GONE

        expandedTitle.text = "Face ID"
        expandedSubtitle.text = "Scanning..."
        expandedIcon.setImageResource(android.R.drawable.ic_menu_view)

        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true

        actionButton1.visibility = View.GONE
        actionButton2.visibility = View.GONE
        actionButton3.visibility = View.GONE

        showIslandWithAnimation()

        expandTimer?.cancel()
        expandTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                expandedSubtitle.text = "Unlocked"
                progressBar.visibility = View.GONE
                hideIsland(500)
            }
        }.start()
    }

    private fun handleAirPodsConnection(intent: Intent) {
        currentActivity = IslandActivity.AIRPODS
        val isConnected = intent.getBooleanExtra("connected", true)
        val deviceName = intent.getStringExtra("deviceName") ?: "AirPods"

        compactIcon1.setImageResource(android.R.drawable.ic_btn_speak_now)
        compactIcon2.visibility = View.GONE

        expandedTitle.text = deviceName
        expandedSubtitle.text = if (isConnected) "Connected" else "Disconnected"
        expandedIcon.setImageResource(android.R.drawable.ic_btn_speak_now)

        progressBar.visibility = View.GONE
        actionButton1.visibility = View.GONE
        actionButton2.visibility = View.GONE
        actionButton3.visibility = View.GONE

        showIslandWithAnimation()
        autoExpandAndCollapse(2000)
    }

    private fun handleRecording(intent: Intent) {
        val isScreenRecording = intent.getBooleanExtra("isScreenRecording", false)
        currentActivity = if (isScreenRecording) IslandActivity.SCREEN_RECORDING else IslandActivity.RECORDING

        compactIcon1.setImageResource(android.R.drawable.ic_menu_camera)
        compactIcon2.visibility = View.VISIBLE
        compactIcon2.setImageResource(android.R.drawable.presence_video_online)

        expandedTitle.text = if (isScreenRecording) "Screen Recording" else "Recording"
        expandedSubtitle.text = "00:00"
        expandedIcon.setImageResource(android.R.drawable.ic_menu_camera)

        actionButton1.setImageResource(android.R.drawable.ic_media_pause)
        actionButton2.setImageResource(android.R.drawable.ic_delete)
        actionButton3.visibility = View.GONE
        actionButton1.visibility = View.VISIBLE
        actionButton2.visibility = View.VISIBLE

        progressBar.visibility = View.GONE

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
        isExpanded = true

        compactView.visibility = View.GONE
        expandedView.visibility = View.VISIBLE
        expandedView.alpha = 0f

        islandContainer.animate()
            .scaleX(1.8f)
            .scaleY(2.5f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        expandedView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay(100)
            .start()
    }

    private fun collapseIsland() {
        isExpanded = false

        expandedView.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                expandedView.visibility = View.GONE
                compactView.visibility = View.VISIBLE
            }
            .start()

        islandContainer.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showIslandWithAnimation() {
        islandContainer.scaleY = 0.3f
        islandContainer.alpha = 0f
        islandContainer.visibility = View.VISIBLE

        islandContainer.animate()
            .scaleY(1.0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun hideIsland(delay: Long = 0) {
        expandTimer?.cancel()

        islandContainer.postDelayed({
            islandContainer.animate()
                .scaleY(0.3f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    islandContainer.visibility = View.GONE
                    currentActivity = IslandActivity.NONE
                    isExpanded = false
                }
                .start()
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
        compactView.visibility = View.VISIBLE
        expandedView.visibility = View.GONE
        islandContainer.visibility = View.GONE
    }

    private fun startTimerCountdown(duration: Long) {
        expandTimer?.cancel()
        expandTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                expandedSubtitle.text = formatTime(millisUntilFinished)
                val progress = ((duration - millisUntilFinished) * 100 / duration).toInt()
                progressBar.progress = progress
            }
            override fun onFinish() {
                expandedSubtitle.text = "00:00"
                progressBar.progress = 100
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
            e.printStackTrace()
        }
    }

    private fun sendMediaAction(action: String) {
        val intent = Intent("com.yourapp.dynamicisland.MEDIA_ACTION")
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
        expandTimer?.cancel()
        unregisterReceiver(updateReceiver)
        if (::islandView.isInitialized) {
            windowManager.removeView(islandView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}