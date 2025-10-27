package com.example.pronounceit

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.os.Looper
import android.graphics.Color
import android.widget.LinearLayout

class HomeActivity : AppCompatActivity() {
    private var previousStreak = 0
    private val streakUpdateManager by lazy { StreakUpdateManager(this) }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var streakDisplay: TextView
    private lateinit var playButton: ImageView
    private lateinit var logoutButton: ImageView
    private lateinit var settingsButton: ImageView
    private lateinit var buttonSound: MediaPlayer
    private lateinit var backgroundMusic: MediaPlayer
    private var isMusicPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI elements
        playButton = findViewById(R.id.playButton)
        logoutButton = findViewById(R.id.logoutButton)
        settingsButton = findViewById(R.id.settingsButton)
        streakDisplay = findViewById(R.id.streakCountText) // Changed from musicToggleButton to streakCountText

        // Initialize button sound
        buttonSound = MediaPlayer.create(this, R.raw.button_click)

        // Initialize background music
        setupBackgroundMusic()

        // Apply bounce animation to the logo
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
        logoImageView.startAnimation(bounceAnimation)

        // Get shared preferences
        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("userId", -1)
        val token = sharedPreferences.getString("token", null)

        if (userId != -1L && token != null) {
            // User is logged in, but no need to fetch details for welcome text anymore
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }

        // Play button with transition animation
        playButton.setOnClickListener {
            // Play button sound
            playButtonSound()

            // Start CategoryActivity after a short delay
            playButton.postDelayed({
                val intent = Intent(this, CategoryActivity::class.java)
                startActivity(intent)
                // Use bubble pop transition when starting CategoryActivity
                overridePendingTransition(R.anim.bubble_pop_in, R.anim.bubble_pop_out)
            }, 100)
        }

        // Settings button with sound
        settingsButton.setOnClickListener {
            playButtonSound()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Logout button with sound
        logoutButton.setOnClickListener {
            playButtonSound()
            if (token != null) {
                showLogoutConfirmationDialog(token)
            } else {
                navigateToLogin()
            }
        }

        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        streakDisplay = findViewById(R.id.streakCountText)

        // Check if coming back from completed lesson
        val lessonCompleted = sharedPreferences.getBoolean("lesson_completed", false)
        if (lessonCompleted) {
            Log.d("HomeActivity", "Lesson completed flag detected, updating streak")
            updateStreakAfterLesson()
            // Reset the flag
            sharedPreferences.edit().putBoolean("lesson_completed", false).apply()
        } else {
            // Just load streak count without updating it
            loadStreakCount()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::backgroundMusic.isInitialized && !backgroundMusic.isPlaying && isMusicPlaying) {
            backgroundMusic.start()
        }

        // If a lesson was just completed while away from HomeActivity, ensure streak is refreshed
        val lessonCompleted = sharedPreferences.getBoolean("lesson_completed", false)
        if (lessonCompleted) {
            Log.d("HomeActivity", "Lesson completed detected on resume, updating streak")
            updateStreakAfterLesson()
            sharedPreferences.edit().putBoolean("lesson_completed", false).apply()
        }
    }

    private fun loadStreakCount() {
        val userId = sharedPreferences.getLong("userId", -1L)
        if (userId != -1L) {
            Log.d("HomeActivity", "Loading streak for user: $userId")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Try to get existing streak
                    val response = RetrofitInstance.getApi(this@HomeActivity)
                        .getStreak(userId)

                    Log.d("HomeActivity", "Get streak response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val streakDTO = response.body()
                        withContext(Dispatchers.Main) {
                            streakDTO?.let {
                                updateStreakDisplay(it.currentStreak)
                            }
                        }
                    } else if (response.code() == 404) {
                        // Streak not found, try to create one
                        Log.d("HomeActivity", "No streak found for user, creating new streak")
                        val createResponse = RetrofitInstance.getApi(this@HomeActivity)
                            .createStreak(userId)

                        Log.d("HomeActivity", "Create streak response code: ${createResponse.code()}")

                        if (!createResponse.isSuccessful) {
                            Log.e("HomeActivity", "Create streak error body: ${createResponse.errorBody()?.string()}")
                        }

                        withContext(Dispatchers.Main) {
                            if (createResponse.isSuccessful) {
                                val streakDTO = createResponse.body()
                                Log.d("HomeActivity", "New streak created: $streakDTO")

                                streakDTO?.let {
                                    updateStreakDisplay(it.currentStreak)
                                }
                            } else {
                                Log.e("HomeActivity", "Failed to create streak: ${createResponse.code()}")
                                // Suppress toast on create failure
                            }
                        }
                    } else {
                        Log.e("HomeActivity", "Failed to load streak: ${response.code()}")
                        Log.e("HomeActivity", "Error body: ${response.errorBody()?.string()}")

                        withContext(Dispatchers.Main) {
                            // Suppress user-facing toast for streak load errors
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Network error loading streak", e)
                    withContext(Dispatchers.Main) {
                        // Use local backup if available; suppress toast
                        val localStreak = sharedPreferences.getInt("local_streak", 0)
                        updateStreakDisplay(localStreak)
                    }
                }
            }
        } else {
            Log.e("HomeActivity", "Cannot load streak: userId is -1")
            // Don't show error toast here as it's confusing during normal app startup
            // Just log the error for debugging
        }
    }

    // Renamed from updateStreak() to updateStreakAfterLesson()
    private fun updateStreakAfterLesson() {
        val userId = sharedPreferences.getLong("userId", -1L)
        if (userId != -1L) {
            Log.d("HomeActivity", "Attempting to update streak for userId: $userId")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Removed automatic markStreakActivity here to avoid incrementing streak before lesson completion.
                    // Streak will now only increment from WordActivity after a successful lesson end.

                    // First get the current streak to save as previous
                    val currentStreakResponse = RetrofitInstance.getApi(this@HomeActivity)
                        .getStreak(userId)

                    Log.d("HomeActivity", "Current streak response code: ${currentStreakResponse.code()}")

                    if (currentStreakResponse.isSuccessful) {
                        // Save the current streak as previous for comparison later
                        val currentStreakDTO = currentStreakResponse.body()
                        previousStreak = currentStreakDTO?.currentStreak ?: 0
                        Log.d("HomeActivity", "Previous streak value: $previousStreak")

                        // Since lesson completion sets the flag and WordActivity marks activity on the server,
                        // simply re-fetch the streak (we already have it) and update UI without calling update again
                        withContext(Dispatchers.Main) {
                            currentStreakDTO?.let {
                                updateStreakDisplay(it.currentStreak)
                                if (it.currentStreak > previousStreak) {
                                    // Delegate popup gating logic to StreakUpdateManager
                                    streakUpdateManager.showStreakUpdateDialog(it)
                                    checkStreakAchievements(it.currentStreak)
                                }
                            }
                        }
                    } else if (currentStreakResponse.code() == 404) {
                        Log.d("HomeActivity", "Streak not found for user, creating new one")
                        // If streak doesn't exist, create a new one
                        val createResponse = RetrofitInstance.getApi(this@HomeActivity)
                            .createStreak(userId)

                        Log.d("HomeActivity", "Create streak response code: ${createResponse.code()}")
                        if (!createResponse.isSuccessful) {
                            Log.e("HomeActivity", "Create streak error body: ${createResponse.errorBody()?.string()}")
                        }

                        withContext(Dispatchers.Main) {
                            if (createResponse.isSuccessful) {
                                val streakDTO = createResponse.body()
                                Log.d("HomeActivity", "New streak created: $streakDTO")

                                streakDTO?.let {
                                    updateStreakDisplay(it.currentStreak)
                                    streakUpdateManager.showStreakUpdateDialog(it)
                                }
                            } else {
                                Log.e("HomeActivity", "Failed to create streak: ${createResponse.code()}")
                                // Suppress toast on create failure
                            }
                        }
                    } else {
                        Log.e("HomeActivity", "Unexpected response when getting streak: ${currentStreakResponse.code()}")
                        Log.e("HomeActivity", "Error body: ${currentStreakResponse.errorBody()?.string()}")

                        withContext(Dispatchers.Main) {
                            // Suppress toast on retrieval error
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error updating streak", e)
                    withContext(Dispatchers.Main) {
                        // Suppress toast on network error; gracefully fall back
                        loadStreakCount() // Fall back to displaying current streak
                    }
                }
            }
        } else {
            Log.e("HomeActivity", "Cannot update streak: userId is -1")
            // Suppress toast: avoid noisy popup when user session is invalid; navigation will handle login state
        }
    }

    private fun checkStreakAchievements(streakCount: Int) {
        // Check for milestone achievements
        when (streakCount) {
            3 -> InAppPopupPoster.postPopupForAchievement("3-Day Streak", 5) // Adjust ID as needed
            7 -> InAppPopupPoster.postPopupForAchievement("7-Day Streak", 6) // Adjust ID as needed
            14 -> InAppPopupPoster.postPopupForAchievement("14-Day Streak", 7) // Adjust ID as needed
            30 -> InAppPopupPoster.postPopupForAchievement("30-Day Streak", 8) // Adjust ID as needed
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateStreakDisplay(days: Int) {
        val streakText = findViewById<TextView>(R.id.streakCountText)
        val streakContainer = findViewById<LinearLayout>(R.id.streakContainer)
        val streakIcon = findViewById<ImageView>(R.id.streakFireIcon)

        // Apply animation if streak increased
        if (days > previousStreak) {
            // Animate the streak text
            val textAnimation = AnimationUtils.loadAnimation(this, R.anim.streak_increment)
            streakText.startAnimation(textAnimation)

            // Animate the fire icon
            val iconAnimation = AnimationUtils.loadAnimation(this, R.anim.streak_flame_pulse)
            streakIcon.startAnimation(iconAnimation)
        }

        // Update the display with bold black text
        streakText.text = days.toString()
        streakText.setTextColor(Color.BLACK)
        streakText.textSize = 20f
        streakText.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)

        previousStreak = days

        // Save streak locally as backup
        sharedPreferences.edit().putInt("local_streak", days).apply()

        // Set click listener on the container for better UX
        streakContainer.setOnClickListener {
            showStreakDetailsDialog(days)
        }
    }

    // Legacy inline popup method removed; use StreakUpdateManager for unified gating & animation.

    private fun showStreakDetailsDialog(days: Int) {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.streak_details_dialog, null)

        // Set up the dialog elements
        val streakIcon = dialogView.findViewById<ImageView>(R.id.streakIconDialog)
        val streakCount = dialogView.findViewById<TextView>(R.id.streakCountDialog)
        val streakMessage = dialogView.findViewById<TextView>(R.id.streakMessageDialog)

        // Configure streak count and message
        streakCount.text = days.toString()
        streakMessage.text = when {
            days == 1 -> "Day Streak!"
            days > 30 -> "Amazing Streak!"
            days > 14 -> "Impressive Streak!"
            days > 7 -> "Great Streak!"
            else -> "Days Streak!"
        }

        // Create dialog with rainbow background
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Make dialog background transparent to show custom background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set animation for dialog appearance
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Apply pulse animation to the fire icon
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.streak_flame_pulse)
        streakIcon.startAnimation(pulseAnimation)

        // Add a slight delay before starting the bounce animation
        Handler(Looper.getMainLooper()).postDelayed({
            val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.fire_bounce)
            streakIcon.startAnimation(bounceAnimation)
        }, 600)

        // Add scale animation to streak count
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.streak_increment)
        streakCount.startAnimation(scaleAnimation)

        // Set click listener to dismiss dialog
        dialogView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(this, R.raw.homemusic)
        backgroundMusic.isLooping = true
        backgroundMusic.setVolume(0.5f, 0.5f)
        backgroundMusic.start()
    }

    // Method to play the button sound
    private fun playButtonSound() {
        if (buttonSound.isPlaying) {
            buttonSound.stop()
            buttonSound.release()
            buttonSound = MediaPlayer.create(this, R.raw.button_click)
        }
        buttonSound.start()
    }

    // Pause background music when activity is not in foreground
    override fun onPause() {
        super.onPause()
        if (::backgroundMusic.isInitialized && backgroundMusic.isPlaying) {
            backgroundMusic.pause()
        }
    }


    // Clean up MediaPlayer resources when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        if (::buttonSound.isInitialized) {
            buttonSound.release()
        }
        if (::backgroundMusic.isInitialized) {
            backgroundMusic.release()
        }
    }

    private fun showLogoutConfirmationDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val logoutMessage = dialogView.findViewById<TextView>(R.id.logoutMessageTextView)
        val logoutButton = dialogView.findViewById<android.widget.Button>(R.id.confirmLogoutButton)
        val cancelButton = dialogView.findViewById<android.widget.Button>(R.id.cancelLogoutButton)

        logoutMessage.text = "Are you sure you want to logout? You will need to sign in again to continue."

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        logoutButton.setOnClickListener {
            // Confirm logout
            dialog.dismiss()
            logout(token)
        }

        cancelButton.setOnClickListener {
            // Cancel logout, dismiss dialog
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logout(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.logout("Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Preserve per-user gating keys (streak + achievements) before clearing everything else
                        val all = sharedPreferences.all
                        val preserved = HashMap<String, Any?>()
                        for ((k, v) in all) {
                            if (k.startsWith("lastStreakPopupDate_u_") ||
                                k.startsWith("lastStreakPopupValue_u_") ||
                                k.startsWith("lastKnownPoints_u_") ||
                                k.startsWith("seenAchievementIds_u_")) {
                                preserved[k] = v
                            }
                        }
                        sharedPreferences.edit().clear().apply()
                        if (preserved.isNotEmpty()) {
                            val editor = sharedPreferences.edit()
                            preserved.forEach { (k, v) ->
                                when (v) {
                                    is String -> editor.putString(k, v)
                                    is Int -> editor.putInt(k, v)
                                }
                            }
                            editor.apply()
                        }

                        // Navigate to LoginActivity
                        navigateToLogin()
                        Toast.makeText(this@HomeActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeActivity, "Failed to log out", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(this, "Please use the logout button to exit", Toast.LENGTH_SHORT).show()
    }
}