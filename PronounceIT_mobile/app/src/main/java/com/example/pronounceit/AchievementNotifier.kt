package com.example.pronounceit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AchievementNotifier {
    private var initialized = false
    private lateinit var appContext: Context
    private val handler = Handler(Looper.getMainLooper())
    // Polling interval lowered to 10s to reduce perceived delay. Consider using FCM for production.
    private val pollIntervalMs = 10_000L // 10s polling for faster responsiveness
    @Volatile
    private var lastManualCheckTs: Long = 0L

    fun initialize(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        initialized = true
        startPolling()
    }

    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                checkForUnlocks()
                handler.postDelayed(this, pollIntervalMs)
            }
        })
    }

    private fun checkForUnlocks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = appContext.getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getLong("userId", -1L)
                val token = prefs.getString("token", "") ?: ""
                if (userId == -1L) return@launch

                // fetch user points
                val userResp = RetrofitInstance.getApi(appContext).getUserById(userId, "Bearer $token")
                val points = if (userResp.isSuccessful) userResp.body()?.accumulatedPoints ?: 0 else 0

                // fetch achievements
                val achievementsResp = RetrofitInstance.getApi(appContext).getAllAchievements()
                val achievements = if (achievementsResp.isSuccessful) achievementsResp.body() ?: emptyList() else emptyList()

                // simple detection: compare persisted lastKnownPoints with current
                val last = prefs.getInt("lastKnownPoints", 0)
                if (points > last) {
                    // find an achievement unlocked in the range
                    val newly = achievements.firstOrNull { ach ->
                        val req = ach.pointsRequired ?: 0
                        req in (last + 1)..points
                    }
                    newly?.let { ach ->
                        // Show a notification via AchievementsActivity helper (it posts notifications)
                        try {
                            // Use AchievementsActivity static-like helper by creating a temp activity intent
                            // But here we'll directly use NotificationUtils: instantiate AchievementsActivity just to call helper isn't ideal.
                            // Instead, post a simple notification using Android APIs.
                            NotificationPoster.post(appContext, ach)
                            // Also try posting an in-app popup on the current foreground activity if any
                            try {
                                InAppPopupPoster.postPopupForAchievement(ach.title ?: "Achievement", ach.id)
                            } catch (e: Exception) {
                                Log.w("AchievementNotifier", "Failed to post in-app popup", e)
                            }

                            // Mark this achievement as seen so we don't re-show it
                            try {
                                val seen = prefs.getStringSet("seenAchievementIds", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                                seen.add(ach.id.toString())
                                prefs.edit().putStringSet("seenAchievementIds", seen).apply()
                            } catch (e: Exception) {
                                Log.w("AchievementNotifier", "Failed to update seenAchievementIds", e)
                            }

                            prefs.edit().putInt("lastKnownPoints", points).apply()
                        } catch (e: Exception) {
                            Log.e("AchievementNotifier", "Failed to post notification", e)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("AchievementNotifier", "Error checking unlocks", e)
            }
        }
    }

    /**
     * Public helper to request an immediate check from other parts of the app (e.g. activity resume).
     */
    fun checkNow() {
        // Debounce manual requests to avoid thundering resumes (2s window)
        val now = System.currentTimeMillis()
        if (now - lastManualCheckTs < 2000L) return
        lastManualCheckTs = now
        // Run the same check logic immediately
        checkForUnlocks()
    }
}
