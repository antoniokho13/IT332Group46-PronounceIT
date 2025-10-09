package com.example.pronounceit

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentActivityRef: WeakReference<Activity>? = null
    @Volatile
    private var suppressAchievementCheck = false

    fun getCurrentActivity(): Activity? {
        return currentActivityRef?.get()
    }

    // Call this to temporarily suppress achievement checks (e.g., during session end dialog flow)
    fun suppressAchievementChecks(durationMs: Long = 10000L) {
        suppressAchievementCheck = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            suppressAchievementCheck = false
        }, durationMs)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        // Only trigger achievement check if not suppressed
        if (!suppressAchievementCheck) {
            try {
                AchievementNotifier.checkNow()
            } catch (e: Exception) {
                // Defensive: don't crash activity lifecycle if notifier misbehaves
            }
        }
    }
    override fun onActivityPaused(activity: Activity) {
        val curr = currentActivityRef?.get()
        if (curr == activity) currentActivityRef = null
    }
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        val curr = currentActivityRef?.get()
        if (curr == activity) currentActivityRef = null
    }
}