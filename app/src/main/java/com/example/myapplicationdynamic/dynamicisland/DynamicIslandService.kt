package com.example.myapplicationdynamic.dynamicisland

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.graphics.Color
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

    enum class IslandActivity {
        NONE, MUSIC, TIMER, CALL, CHARGING, FACE_UNLOCK, AIRPODS, RECORDING, SCREEN_RECORDING, ALARM
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "========== SERVICE STARTING ==========")

        try {
            // Create notification channel and start foreground
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            Log.d(TAG, "✓ Foreground service started")

            // Get window manager
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            Log.d(TAG, "✓ WindowManager obtained")

            // Create and add the island view
            createIslandView()

            // Wait a bit then show test island
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

    private fun createIslandView() {
        try {
            Log.d(TAG, "Creating island view...")

            // Create view programmatically to avoid inflation issues
            islandContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.BLACK)
                setPadding(40, 30, 40, 30)
                elevation = 10f
            }

            // Create compact view
            compactView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            compactIcon1 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(60, 60)
                setColorFilter(Color.WHITE)
                setImageResource(android.R.drawable.ic_media_play)
            }

            compactIcon2 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(60, 60).apply {
                    marginStart = 20
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
                layoutParams = LinearLayout.LayoutParams(800, LinearLayout.LayoutParams.WRAP_CONTENT)
                visibility = View.GONE
            }

            // Expanded content
            val expandedContent = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            expandedIcon = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                setColorFilter(Color.WHITE)
                setImageResource(android.R.drawable.ic_media_play)
            }

            val textContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 40
                }
            }

            expandedTitle = TextView(this).apply {
                text = "Test Song"
                textSize = 18f
                setTextColor(Color.WHITE)
            }

            expandedSubtitle = TextView(this).apply {
                text = "Test Artist"
                textSize = 14f
                setTextColor(Color.LTGRAY)
            }

            textContainer.addView(expandedTitle)
            textContainer.addView(expandedSubtitle)

            expandedContent.addView(expandedIcon)
            expandedContent.addView(textContainer)

            // Progress bar
            progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    10
                ).apply {
                    topMargin = 40
                }
                visibility = View.GONE
            }

            // Action buttons
            val actionsContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 40
                }
            }

            actionButton1 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    marginEnd = 40
                }
                setColorFilter(Color.WHITE)
                visibility = View.GONE
            }

            actionButton2 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    marginEnd = 40
                }
                setColorFilter(Color.WHITE)
                visibility = View.GONE
            }

            actionButton3 = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                setColorFilter(Color.WHITE)
                visibility = View.GONE
            }

            actionsContainer.addView(actionButton1)
            actionsContainer.addView(actionButton2)
            actionsContainer.addView(actionButton3)

            expandedView?.addView(expandedContent)
            expandedView?.addView(progressBar)
            expandedView?.addView(actionsContainer)

            // Add views to container
            islandContainer?.addView(compactView)
            islandContainer?.addView(expandedView)

            // Set up click listener
            islandContainer?.setOnClickListener {
                Log.d(TAG, "Island clicked!")
                toggleExpansion()
            }

            // Create window layout parameters
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
                y = 50
            }

            // Add view to window
            windowManager?.addView(islandContainer, params)
            isViewAdded = true

            Log.d(TAG, "✓ Island view created and added successfully!")
            Toast.makeText(this, "Dynamic Island Ready!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "✗ ERROR creating island view", e)
            e.printStackTrace()
            Toast.makeText(this, "Error creating island: ${e.message}", Toast.LENGTH_LONG).show()
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

            // Make it visible
            islandContainer?.visibility = View.VISIBLE
            islandContainer?.alpha = 1f

            Toast.makeText(this, "Test island showing!", Toast.LENGTH_SHORT).show()

            // Auto expand after 2 seconds
            android.os.Handler(mainLooper).postDelayed({
                expandIsland()
            }, 2000)

        } catch (e: Exception) {
            Log.e(TAG, "Error showing test island", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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

        compactView?.visibility = View.GONE
        expandedView?.visibility = View.VISIBLE
        expandedView?.alpha = 0f

        expandedView?.animate()
            ?.alpha(1f)
            ?.setDuration(300)
            ?.start()

        Toast.makeText(this, "Island Expanded", Toast.LENGTH_SHORT).show()
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

        Toast.makeText(this, "Island Collapsed", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroying...")
        expandTimer?.cancel()

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