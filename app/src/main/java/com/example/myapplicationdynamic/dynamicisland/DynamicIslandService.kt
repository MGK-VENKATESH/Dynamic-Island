package com.example.myapplicationdynamic.dynamicisland

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplicationdynamic.R

class DynamicIslandService : Service() {

    companion object {
        private const val TAG = "DynamicIslandService"
        private const val CHANNEL_ID = "DynamicIslandChannel"
        private const val NOTIFICATION_ID = 1
    }

    private var windowManager: WindowManager? = null
    private var islandView: View? = null
    private var islandContainer: LinearLayout? = null
    private var compactView: LinearLayout? = null
    private var expandedView: LinearLayout? = null

    private var compactIcon1: ImageView? = null
    private var compactIcon2: ImageView? = null
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

    // Settings
    private lateinit var prefs: SharedPreferences
    private var islandPositionY = 20
    private var islandSize = 50
    private var autoExpandEnabled = true
    private var vibrateEnabled = true
    private var hapticFeedback: HapticFeedback? = null

    // Broadcast receiver for island updates
    private val islandUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { handleIslandUpdate(it) }
        }
    }

    enum class IslandActivity {
        NONE, MUSIC, TIMER, CALL, CHARGING, FACE_UNLOCK, AIRPODS, RECORDING, SCREEN_RECORDING, ALARM
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "========== SERVICE STARTING ==========")

        try {
            // Load settings
            prefs = getSharedPreferences("DynamicIslandSettings", Context.MODE_PRIVATE)
            loadSettings()

            // Initialize haptic feedback
            if (vibrateEnabled) {
                hapticFeedback = HapticFeedback(this)
            }

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            Log.d(TAG, "✓ Foreground service started")

            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            Log.d(TAG, "✓ WindowManager obtained")

            createIslandView()

            // Register broadcast receiver
            val filter = IntentFilter().apply {
                addAction("com.yourapp.dynamicisland.UPDATE_ISLAND")
                addAction("com.yourapp.dynamicisland.SHOW_CALL")
                addAction("com.yourapp.dynamicisland.SHOW_TIMER")
                addAction("com.yourapp.dynamicisland.SHOW_MUSIC")
            }
            registerReceiver(islandUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
            Log.d(TAG, "✓ Broadcast receiver registered")

            // Show test island after delay
            android.os.Handler(mainLooper).postDelayed({
                Log.d(TAG, "Showing test island...")
                showTestIsland()
            }, 2000)

        } catch (e: Exception) {
            Log.e(TAG, "✗ CRITICAL ERROR in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun loadSettings() {
        islandPositionY = prefs.getInt("position", 20)
        islandSize = prefs.getInt("size", 50)
        autoExpandEnabled = prefs.getBoolean("autoExpand", true)
        vibrateEnabled = prefs.getBoolean("vibrate", true)

        Log.d(TAG, "Settings loaded: position=$islandPositionY, size=$islandSize, autoExpand=$autoExpandEnabled, vibrate=$vibrateEnabled")
    }

    private fun handleIslandUpdate(intent: Intent) {
        val type = intent.getStringExtra("type") ?: return

        Log.d(TAG, "Island update received: $type")

        when (type) {
            "hide" -> hideIsland()
            "music" -> {
                if (prefs.getBoolean("showMusic", true)) {
                    showMusicPlayer(intent)
                }
            }
            "call" -> {
                if (prefs.getBoolean("showCalls", true)) {
                    showCall(intent)
                }
            }
            "charging" -> {
                if (prefs.getBoolean("showCharging", true)) {
                    showCharging(intent)
                }
            }
            "timer" -> {
                if (prefs.getBoolean("showTimer", true)) {
                    showTimer(intent)
                }
            }
            "airpods" -> {
                if (prefs.getBoolean("showBluetooth", true)) {
                    showAirPods(intent)
                }
            }
            "faceunlock" -> {
                if (prefs.getBoolean("showFaceUnlock", true)) {
                    showFaceUnlock()
                }
            }
        }
    }

    private fun showMusicPlayer(intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Unknown"
        val artist = intent.getStringExtra("artist") ?: "Unknown Artist"
        val isPlaying = intent.getBooleanExtra("isPlaying", true)

        expandedTitle?.text = title
        expandedSubtitle?.text = artist
        compactIcon1?.setImageResource(if (isPlaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
        expandedIcon?.setImageResource(android.R.drawable.ic_media_play)

        showIsland()
        if (autoExpandEnabled) {
            android.os.Handler(mainLooper).postDelayed({
                if (!isExpanded) expandIsland()
            }, 1500)
        }
    }

    private fun showCall(intent: Intent) {
        val contactName = intent.getStringExtra("contactName") ?: "Unknown"
        val duration = intent.getStringExtra("duration") ?: "00:00"
        val isIncoming = intent.getBooleanExtra("isIncoming", true)

        expandedTitle?.text = if (isIncoming) "Incoming Call" else "Call"
        expandedSubtitle?.text = "$contactName • $duration"
        compactIcon1?.setImageResource(android.R.drawable.stat_sys_phone_call)
        expandedIcon?.setImageResource(android.R.drawable.stat_sys_phone_call)

        showIsland()
        if (autoExpandEnabled) {
            expandIsland()
        }
    }

    private fun showCharging(intent: Intent) {
        val batteryLevel = intent.getIntExtra("batteryLevel", 0)

        expandedTitle?.text = "Charging"
        expandedSubtitle?.text = "$batteryLevel%"
        compactIcon1?.setImageResource(android.R.drawable.ic_lock_idle_charging)
        expandedIcon?.setImageResource(android.R.drawable.ic_lock_idle_charging)
        progressBar?.visibility = View.VISIBLE
        progressBar?.progress = batteryLevel

        showIsland()
        if (autoExpandEnabled) {
            android.os.Handler(mainLooper).postDelayed({
                if (!isExpanded) expandIsland()
            }, 1500)
        }
    }

    private fun showTimer(intent: Intent) {
        val duration = intent.getStringExtra("duration") ?: "00:00"

        expandedTitle?.text = "Timer"
        expandedSubtitle?.text = duration
        compactIcon1?.setImageResource(android.R.drawable.ic_menu_recent_history)
        expandedIcon?.setImageResource(android.R.drawable.ic_menu_recent_history)

        showIsland()
        if (autoExpandEnabled) {
            android.os.Handler(mainLooper).postDelayed({
                if (!isExpanded) expandIsland()
            }, 1500)
        }
    }

    private fun showAirPods(intent: Intent) {
        val deviceName = intent.getStringExtra("deviceName") ?: "Bluetooth Device"
        val connected = intent.getBooleanExtra("connected", true)

        expandedTitle?.text = if (connected) "Connected" else "Disconnected"
        expandedSubtitle?.text = deviceName
        compactIcon1?.setImageResource(android.R.drawable.stat_sys_data_bluetooth)
        expandedIcon?.setImageResource(android.R.drawable.stat_sys_data_bluetooth)

        showIsland()
        if (autoExpandEnabled) {
            android.os.Handler(mainLooper).postDelayed({
                if (!isExpanded) expandIsland()
            }, 1500)
        }

        // Auto hide after 3 seconds
        android.os.Handler(mainLooper).postDelayed({
            hideIsland()
        }, 3000)
    }

    private fun showFaceUnlock() {
        expandedTitle?.text = "Face ID"
        expandedSubtitle?.text = "Scanning..."
        compactIcon1?.setImageResource(android.R.drawable.ic_lock_lock)
        expandedIcon?.setImageResource(android.R.drawable.ic_lock_lock)

        showIsland()

        // Simulate unlock after 1 second
        android.os.Handler(mainLooper).postDelayed({
            expandedSubtitle?.text = "Unlocked"
            android.os.Handler(mainLooper).postDelayed({
                hideIsland()
            }, 1500)
        }, 1000)
    }

    private fun showIsland() {
        islandContainer?.visibility = View.VISIBLE
        islandContainer?.alpha = 0f
        islandContainer?.scaleY = 0.5f

        islandContainer?.animate()
            ?.alpha(1f)
            ?.scaleY(1f)
            ?.setDuration(400)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()

        if (vibrateEnabled) {
            hapticFeedback?.notificationFeedback()
        }
    }

    private fun hideIsland() {
        islandContainer?.animate()
            ?.alpha(0f)
            ?.scaleY(0.5f)
            ?.setDuration(300)
            ?.withEndAction {
                islandContainer?.visibility = View.GONE
                if (isExpanded) {
                    collapseIsland()
                }
            }
            ?.start()
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

    private fun dpToPx(dp: Int): Int {
        val scaledDp = (dp * islandSize / 50f).toInt()
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            scaledDp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun createRoundedBackground(cornerRadius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.BLACK)
            setCornerRadius(cornerRadius)
        }
    }

    private fun createIslandView() {
        try {
            Log.d(TAG, "Creating island view...")

            // Main container with rounded corners
            islandContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = createRoundedBackground(dpToPx(40).toFloat())
                setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                elevation = dpToPx(16).toFloat()
            }

            // Create compact view
            compactView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            compactIcon1 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24))
                setColorFilter(Color.WHITE)
                setImageResource(android.R.drawable.ic_media_play)
            }

            compactIcon2 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                    marginStart = dpToPx(12)
                }
                setColorFilter(Color.WHITE)
                setImageResource(android.R.drawable.ic_media_play)
                visibility = View.GONE
            }

            compactView?.addView(compactIcon1)
            compactView?.addView(compactIcon2)

            // Create expanded view
            expandedView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(280),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                visibility = View.GONE
            }

            // Expanded header
            val expandedHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            expandedIcon = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(48), dpToPx(48))
                setColorFilter(Color.WHITE)
                setImageResource(android.R.drawable.ic_media_play)
                background = createRoundedBackground(dpToPx(12).toFloat()).apply {
                    setColor(Color.parseColor("#222222"))
                }
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }

            val textContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = dpToPx(16)
                }
            }

            expandedTitle = TextView(this).apply {
                text = "Now Playing"
                textSize = 16f * (islandSize / 50f)
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            expandedSubtitle = TextView(this).apply {
                text = "Test Song • Test Artist"
                textSize = 13f * (islandSize / 50f)
                setTextColor(Color.parseColor("#CCCCCC"))
                setPadding(0, dpToPx(4), 0, 0)
            }

            textContainer.addView(expandedTitle)
            textContainer.addView(expandedSubtitle)

            expandedHeader.addView(expandedIcon)
            expandedHeader.addView(textContainer)

            // Progress bar
            progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(4)
                ).apply {
                    topMargin = dpToPx(16)
                }
                progressDrawable = createRoundedBackground(dpToPx(2).toFloat()).apply {
                    setColor(Color.parseColor("#FFFFFF"))
                }
                background = createRoundedBackground(dpToPx(2).toFloat()).apply {
                    setColor(Color.parseColor("#333333"))
                }
                progress = 45
                visibility = View.GONE
            }

            // Action buttons container
            val actionsContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(16)
                }
            }

            actionButton1 = createActionButton(android.R.drawable.ic_media_previous)
            actionButton2 = createActionButton(android.R.drawable.ic_media_pause)
            actionButton3 = createActionButton(android.R.drawable.ic_media_next)

            actionsContainer.addView(actionButton1)
            actionsContainer.addView(actionButton2)
            actionsContainer.addView(actionButton3)

            expandedView?.addView(expandedHeader)
            expandedView?.addView(progressBar)
            expandedView?.addView(actionsContainer)

            // Add views to container
            islandContainer?.addView(compactView)
            islandContainer?.addView(expandedView)

            // Click listener
            islandContainer?.setOnClickListener {
                Log.d(TAG, "Island clicked!")
                if (vibrateEnabled) {
                    hapticFeedback?.tapFeedback()
                }
                toggleExpansion()
            }

            // Window parameters
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = dpToPx(islandPositionY)
            }

            windowManager?.addView(islandContainer, params)
            isViewAdded = true

            Log.d(TAG, "✓ Island view created successfully!")
            Toast.makeText(this, "Dynamic Island Ready! ✨", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "✗ ERROR creating island view", e)
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createActionButton(iconRes: Int): ImageView {
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(44), dpToPx(44)).apply {
                setMargins(dpToPx(8), 0, dpToPx(8), 0)
            }
            setImageResource(iconRes)
            setColorFilter(Color.WHITE)
            background = createRoundedBackground(dpToPx(22).toFloat()).apply {
                setColor(Color.parseColor("#333333"))
            }
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            scaleType = ImageView.ScaleType.FIT_CENTER
            visibility = View.VISIBLE

            setOnClickListener {
                if (vibrateEnabled) {
                    hapticFeedback?.tapFeedback()
                }
                animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(100)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
        }
    }

    private fun showTestIsland() {
        try {
            if (!isViewAdded || islandContainer == null) {
                Log.e(TAG, "Cannot show test - view not ready")
                return
            }

            Log.d(TAG, "Showing test island...")
            currentActivity = IslandActivity.MUSIC

            islandContainer?.visibility = View.VISIBLE
            islandContainer?.alpha = 0f
            islandContainer?.scaleY = 0.5f

            islandContainer?.animate()
                ?.alpha(1f)
                ?.scaleY(1f)
                ?.setDuration(400)
                ?.setInterpolator(AccelerateDecelerateInterpolator())
                ?.start()

            if (vibrateEnabled) {
                hapticFeedback?.notificationFeedback()
            }

            Toast.makeText(this, "Tap to expand/collapse", Toast.LENGTH_SHORT).show()

            // Auto expand if enabled
            if (autoExpandEnabled) {
                android.os.Handler(mainLooper).postDelayed({
                    if (!isExpanded) {
                        expandIsland()
                    }
                }, 2500)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error showing test island", e)
        }
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

        if (vibrateEnabled) {
            hapticFeedback?.expandFeedback()
        }

        compactView?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                compactView?.visibility = View.GONE
                expandedView?.visibility = View.VISIBLE
                expandedView?.alpha = 0f

                expandedView?.animate()
                    ?.alpha(1f)
                    ?.setDuration(300)
                    ?.start()
            }
            ?.start()

        islandContainer?.animate()
            ?.scaleX(1.1f)
            ?.setDuration(400)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }

    private fun collapseIsland() {
        Log.d(TAG, "Collapsing island")
        isExpanded = false

        if (vibrateEnabled) {
            hapticFeedback?.collapseFeedback()
        }

        expandedView?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                expandedView?.visibility = View.GONE
                compactView?.visibility = View.VISIBLE
                compactView?.alpha = 0f

                compactView?.animate()
                    ?.alpha(1f)
                    ?.setDuration(300)
                    ?.start()
            }
            ?.start()

        islandContainer?.animate()
            ?.scaleX(1.0f)
            ?.setDuration(400)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroying...")
        expandTimer?.cancel()

        try {
            unregisterReceiver(islandUpdateReceiver)
            Log.d(TAG, "✓ Broadcast receiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        if (isViewAdded && islandContainer != null) {
            try {
                windowManager?.removeView(islandContainer)
                isViewAdded = false
                Log.d(TAG, "✓ Island view removed")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing view", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}