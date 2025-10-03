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
                val pointsKey = "lastKnownPoints_u_$userId"
                val seenSetKey = "seenAchievementIds_u_$userId"

                // Migration: If legacy global keys exist and per-user keys don't, migrate.
                if (!prefs.contains(pointsKey) && prefs.contains("lastKnownPoints")) {
                    val legacyPoints = prefs.getInt("lastKnownPoints", 0)
                    prefs.edit().remove("lastKnownPoints").putInt(pointsKey, legacyPoints).apply()
                }
                if (!prefs.contains(seenSetKey) && prefs.contains("seenAchievementIds")) {
                    val legacySet = prefs.getStringSet("seenAchievementIds", mutableSetOf()) ?: mutableSetOf()
                    prefs.edit().remove("seenAchievementIds").putStringSet(seenSetKey, legacySet).apply()
                }

                // fetch user points (current)
                val userResp = RetrofitInstance.getApi(appContext).getUserById(userId, "Bearer $token")
                val points = if (userResp.isSuccessful) userResp.body()?.accumulatedPoints ?: 0 else 0

                // fetch achievements (active or all) - using existing endpoint
                val achievementsResp = RetrofitInstance.getApi(appContext).getAllAchievements()
                val achievements = if (achievementsResp.isSuccessful) achievementsResp.body() ?: emptyList() else emptyList()

                // Compare per-user lastKnownPoints
                val last = prefs.getInt(pointsKey, 0)
                if (points <= last) {
                    // No new points that cross a threshold
                    return@launch
                }

                // Retrieve previously seen achievements for this user
                val seen = prefs.getStringSet(seenSetKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

                // Find the lowest threshold achievement newly crossed that hasn't been seen
                val newly = achievements
                    .filter { ach -> ach.pointsRequired in (last + 1)..points }
                    .sortedBy { it.pointsRequired }
                    .firstOrNull { !seen.contains(it.id.toString()) }

                if (newly != null) {
                    try {
                        NotificationPoster.post(appContext, newly)
                        try {
                            InAppPopupPoster.postPopupForAchievement(newly.title, newly.id)
                        } catch (e: Exception) {
                            Log.w("AchievementNotifier", "In-app popup failed", e)
                        }
                        // Mark seen and update last points AFTER successful post
                        seen.add(newly.id.toString())
                        prefs.edit()
                            .putStringSet(seenSetKey, seen)
                            .putInt(pointsKey, points)
                            .apply()
                    } catch (e: Exception) {
                        Log.e("AchievementNotifier", "Failed to post notification", e)
                    }
                } else {
                    // No unseen achievements unlocked; still advance lastKnownPoints so we don't re-scan same range repeatedly
                    prefs.edit().putInt(pointsKey, points).apply()
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
