package com.example.pronounceit

import android.app.Application
import android.util.Log

class PronounceItApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Register lifecycle callbacks to know which Activity is currently resumed
        registerActivityLifecycleCallbacks(ActivityTracker)
        try {
            AchievementNotifier.initialize(this)
        } catch (e: Exception) {
            Log.e("PronounceItApp", "Failed to initialize AchievementNotifier", e)
        }
    }
}
