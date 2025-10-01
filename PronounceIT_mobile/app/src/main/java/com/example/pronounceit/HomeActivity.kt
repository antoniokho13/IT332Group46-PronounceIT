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
import android.widget.FrameLayout
import android.graphics.Color
import android.widget.LinearLayout
import android.view.animation.AnimationSet

class HomeActivity : AppCompatActivity() {
    private var previousStreak = 0
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
                logout(token)
            } else {
                navigateToLogin()
            }
        }

        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        streakDisplay = findViewById(R.id.streakCountText)

        // Check if coming back from completed lesson
        val lessonCompleted = sharedPreferences.getBoolean("lesson_completed", false)
        if (lessonCompleted) {
            updateStreak()
            // Reset the flag
            sharedPreferences.edit().putBoolean("lesson_completed", false).apply()
        } else {
            loadStreakCount()
        }
    }

    private fun loadStreakCount() {
        val userId = sharedPreferences.getLong("userId", -1L)
        if (userId != -1L) {
            Log.d("HomeActivity", "Loading streak for user: $userId")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Try to get existing streak
                    var response = RetrofitInstance.getApi(this@HomeActivity)
                        .getStreak(userId)

                    if (response.code() == 404) {
                        // If streak doesn't exist, create a new one
                        response = RetrofitInstance.getApi(this@HomeActivity)
                            .createStreak(userId)
                    }

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val streakDTO = response.body()
                            Log.d("HomeActivity", "Streak data received: $streakDTO")
                            if (streakDTO != null) {
                                previousStreak = streakDTO.currentStreak
                                updateStreakDisplay(streakDTO.currentStreak)
                            } else {
                                Log.e("HomeActivity", "Streak DTO is null")
                                updateStreakDisplay(0)
                            }
                        } else {
                            Log.e("HomeActivity", "Failed to load/create streak: ${response.code()}")
                            updateStreakDisplay(0)
                            showErrorToast("Failed to load streak data")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Network error", e)
                    withContext(Dispatchers.Main) {
                        updateStreakDisplay(0)
                        showErrorToast("Network error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun updateStreak() {
        val userId = sharedPreferences.getLong("userId", -1L)
        if (userId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // First get the current streak to save as previous
                    val currentStreakResponse = RetrofitInstance.getApi(this@HomeActivity)
                        .getStreak(userId)

                    if (currentStreakResponse.isSuccessful) {
                        // Save the current streak as previous for comparison later
                        val currentStreakDTO = currentStreakResponse.body()
                        previousStreak = currentStreakDTO?.currentStreak ?: 0

                        // Now update the streak
                        val updateResponse = RetrofitInstance.getApi(this@HomeActivity)
                            .updateStreak(userId)

                        withContext(Dispatchers.Main) {
                            if (updateResponse.isSuccessful) {
                                val updatedStreakDTO = updateResponse.body()
                                Log.d("HomeActivity", "Streak updated: $updatedStreakDTO")

                                updatedStreakDTO?.let {
                                    // Update UI
                                    updateStreakDisplay(it.currentStreak)

                                    // Check if streak increased and show animation/dialog if it did
                                    if (it.currentStreak > previousStreak) {
                                        showStreakUpdateDialog(it.currentStreak)

                                        // Check for milestone achievements
                                        checkStreakAchievements(it.currentStreak)
                                    }
                                }
                            } else {
                                Log.e("HomeActivity", "Failed to update streak: ${updateResponse.code()}")
                                showErrorToast("Failed to update streak")
                                loadStreakCount() // Try to at least display current streak
                            }
                        }
                    } else if (currentStreakResponse.code() == 404) {
                        // If streak doesn't exist, create a new one
                        val createResponse = RetrofitInstance.getApi(this@HomeActivity)
                            .createStreak(userId)

                        withContext(Dispatchers.Main) {
                            if (createResponse.isSuccessful) {
                                val streakDTO = createResponse.body()
                                Log.d("HomeActivity", "New streak created: $streakDTO")

                                streakDTO?.let {
                                    updateStreakDisplay(it.currentStreak)
                                    showStreakUpdateDialog(it.currentStreak)
                                }
                            } else {
                                Log.e("HomeActivity", "Failed to create streak: ${createResponse.code()}")
                                showErrorToast("Failed to create streak")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error updating streak", e)
                    withContext(Dispatchers.Main) {
                        showErrorToast("Network error: ${e.message}")
                        loadStreakCount() // Fall back to displaying current streak
                    }
                }
            }
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

    private fun showStreakUpdateDialog(days: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.streak_update_dialog, null)

        val dialog = AlertDialog.Builder(this, R.style.StreakDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Update to use fire icon for streak dialog
        val streakIcon = dialogView.findViewById<ImageView>(R.id.streakIcon)
        streakIcon.setImageResource(R.drawable.fire_streak_icon)

        // Customize dialog content
        dialogView.apply {
            findViewById<TextView>(R.id.streakMessage).text = "Streak Increased!"
            findViewById<TextView>(R.id.streakSubMessage).text =
                "You're on fire! $days days of continuous learning"
        }

        // Add animation to dialog
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Add animation to the fire icon in dialog
        val iconAnimation = AnimationUtils.loadAnimation(this, R.anim.streak_flame_pulse)
        streakIcon.startAnimation(iconAnimation)

        dialog.show()

        // Auto dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }

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

    // Resume background music when activity returns to foreground
    override fun onResume() {
        super.onResume()
        if (::backgroundMusic.isInitialized && !backgroundMusic.isPlaying && isMusicPlaying) {
            backgroundMusic.start()
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

    private fun logout(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.logout("Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Clear shared preferences
                        sharedPreferences.edit().clear().apply()

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