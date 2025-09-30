package com.example.pronounceit

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentActivityRef: WeakReference<Activity>? = null

    fun getCurrentActivity(): Activity? {
        return currentActivityRef?.get()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        // Trigger an immediate achievement check when the user resumes any activity
        try {
            AchievementNotifier.checkNow()
        } catch (e: Exception) {
            // Defensive: don't crash activity lifecycle if notifier misbehaves
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
