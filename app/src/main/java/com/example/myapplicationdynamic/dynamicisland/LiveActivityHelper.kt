package com.example.myapplicationdynamic.dynamicisland



import android.content.Context
import java.util.*

class LiveActivityHelper(private val context: Context) {

    private val activeActivities = mutableListOf<ActivityData>()

    data class ActivityData(
        val id: String,
        val type: String,
        val title: String,
        val subtitle: String,
        val priority: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun addActivity(activity: ActivityData) {
        // Remove if already exists
        activeActivities.removeIf { it.id == activity.id }

        // Add new activity
        activeActivities.add(activity)

        // Sort by priority (higher priority first)
        activeActivities.sortByDescending { it.priority }

        // Show highest priority activity
        showCurrentActivity()
    }

    fun removeActivity(id: String) {
        activeActivities.removeIf { it.id == id }
        showCurrentActivity()
    }

    fun clearAllActivities() {
        activeActivities.clear()
    }

    private fun showCurrentActivity() {
        if (activeActivities.isEmpty()) {
            // Hide island
            val intent = android.content.Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
            intent.putExtra("type", "hide")
            context.sendBroadcast(intent)
            return
        }

        val topActivity = activeActivities.first()
        val intent = android.content.Intent("com.yourapp.dynamicisland.UPDATE_ISLAND")
        intent.putExtra("type", topActivity.type)
        intent.putExtra("title", topActivity.title)
        intent.putExtra("subtitle", topActivity.subtitle)
        context.sendBroadcast(intent)
    }

    fun getActiveCount(): Int = activeActivities.size

    fun hasActiveActivities(): Boolean = activeActivities.isNotEmpty()

    // Priority levels
    companion object {
        const val PRIORITY_URGENT = 100 // Calls, Alarms
        const val PRIORITY_HIGH = 80    // Timers, Recording
        const val PRIORITY_MEDIUM = 50  // Music, Charging
        const val PRIORITY_LOW = 20     // Bluetooth, Face Unlock
    }
}