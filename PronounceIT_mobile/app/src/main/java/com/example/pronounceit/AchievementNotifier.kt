package com.example.pronounceit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.AchievementEntity
import com.example.pronounceit.utils.AchievementCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AchievementNotifier {
    private var initialized = false
    private lateinit var appContext: Context
    private val handler = Handler(Looper.getMainLooper())
    private val pollIntervalMs = 10_000L // 10s polling for faster responsiveness
    @Volatile
    private var lastManualCheckTs: Long = 0L

    // Fields to handle suppression
    private val pendingAchievements = mutableListOf<AchievementEntity>()
    private var suppressPopups = false

    fun initialize(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        initialized = true
        startPolling()
    }

    // Methods to suppress/allow popups
    fun suppressPopups() {
        suppressPopups = true
    }

    fun allowPopups() {
        suppressPopups = false
        showPendingAchievements()
    }

    // Show any achievements that were earned while suppressed
    private fun showPendingAchievements() {
        if (pendingAchievements.isEmpty()) return

        CoroutineScope(Dispatchers.Main).launch {
            val toShow = ArrayList(pendingAchievements)
            pendingAchievements.clear()

            // Show each pending achievement with a delay between them
            toShow.forEachIndexed { index, achievement ->
                if (index > 0) {
                    kotlinx.coroutines.delay(1500) // 1.5s between popups
                }
                try {
                    NotificationPoster.post(appContext, achievement)
                    InAppPopupPoster.postPopupForAchievement(achievement.title, achievement.id)
                } catch (e: Exception) {
                    Log.e("AchievementNotifier", "Error showing pending achievement", e)
                }
            }
        }
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

                // Migration code remains unchanged
                if (!prefs.contains(pointsKey) && prefs.contains("lastKnownPoints")) {
                    val legacyPoints = prefs.getInt("lastKnownPoints", 0)
                    prefs.edit().remove("lastKnownPoints").putInt(pointsKey, legacyPoints).apply()
                }
                if (!prefs.contains(seenSetKey) && prefs.contains("seenAchievementIds")) {
                    val legacySet = prefs.getStringSet("seenAchievementIds", mutableSetOf()) ?: mutableSetOf()
                    prefs.edit().remove("seenAchievementIds").putStringSet(seenSetKey, legacySet).apply()
                }

                val userResp = RetrofitInstance.getApi(appContext).getUserById(userId, "Bearer $token")
                val points = if (userResp.isSuccessful) userResp.body()?.accumulatedPoints ?: 0 else 0

                // Use cached achievements to reduce API calls
                val achievements = AchievementCache
                    .getAchievements(appContext)

                val last = prefs.getInt(pointsKey, 0)
                if (points <= last) {
                    return@launch
                }

                val seen = prefs.getStringSet(seenSetKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

                val newly = achievements
                    .filter { ach -> ach.pointsRequired in (last + 1)..points }
                    .sortedBy { it.pointsRequired }
                    .firstOrNull { !seen.contains(it.id.toString()) }

                if (newly != null) {
                    // Mark as seen immediately to prevent duplicates
                    seen.add(newly.id.toString())
                    prefs.edit()
                        .putStringSet(seenSetKey, seen)
                        .putInt(pointsKey, points)
                        .apply()

                    // Show popup immediately or store it for later
                    if (suppressPopups) {
                        // Store for later display
                        synchronized(pendingAchievements) {
                            if (!pendingAchievements.any { it.id == newly.id }) {
                                pendingAchievements.add(newly)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            try {
                                NotificationPoster.post(appContext, newly)
                                InAppPopupPoster.postPopupForAchievement(newly.title, newly.id)
                            } catch (e: Exception) {
                                Log.e("AchievementNotifier", "Failed to post notification", e)
                            }
                        }
                    }
                } else {
                    prefs.edit().putInt(pointsKey, points).apply()
                }
            } catch (e: Exception) {
                Log.e("AchievementNotifier", "Error checking unlocks", e)
            }
        }
    }

    fun checkNow() {
        val now = System.currentTimeMillis()
        if (now - lastManualCheckTs < 2000L) return
        lastManualCheckTs = now
        checkForUnlocks()
    }
}