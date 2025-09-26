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
                    // First try to create a new streak
                    var response = RetrofitInstance.getApi(this@HomeActivity)
                        .createStreak(userId)

                    // If creation fails (streak might exist), try to get existing streak
                    if (!response.isSuccessful) {
                        response = RetrofitInstance.getApi(this@HomeActivity)
                            .getStreak(userId)
                    }

                    withContext(Dispatchers.Main) {
                        when (response.code()) {
                            200 -> {
                                val streakDTO = response.body()
                                Log.d("HomeActivity", "Streak data received: $streakDTO")
                                if (streakDTO != null) {
                                    previousStreak = streakDTO.currentStreak
                                    updateStreakDisplay(streakDTO.currentStreak)
                                } else {
                                    Log.e("HomeActivity", "Streak DTO is null")
                                    updateStreakDisplay(0)
                                }
                            }
                            404 -> {
                                Log.d("HomeActivity", "No streak found - creating new one")
                                createNewStreak(userId)
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("HomeActivity", "Server error: ${response.code()}, $errorBody")
                                updateStreakDisplay(0)
                                showErrorToast("Failed to load streak data")
                            }
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

    private fun createNewStreak(userId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.getApi(this@HomeActivity)
                    .createStreak(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val streakDTO = response.body()
                        if (streakDTO != null) {
                            previousStreak = streakDTO.currentStreak
                            updateStreakDisplay(streakDTO.currentStreak)
                        }
                    } else {
                        Log.e("HomeActivity", "Failed to create streak: ${response.code()}")
                        updateStreakDisplay(0)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error creating streak", e)
                withContext(Dispatchers.Main) {
                    updateStreakDisplay(0)
                }
            }
        }
    }

    private fun updateStreak() {
        val userId = sharedPreferences.getLong("userId", -1L)
        if (userId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.getApi(this@HomeActivity)
                        .updateStreak(userId)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val streakDTO = response.body()
                            streakDTO?.let {
                                if (it.currentStreak > previousStreak) {
                                    showStreakUpdateDialog(it.currentStreak)
                                }
                                updateStreakDisplay(it.currentStreak)
                            }
                        } else {
                            Log.e("HomeActivity", "Failed to update streak: ${response.code()}")
                            showErrorToast("Failed to update streak")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error updating streak", e)
                    withContext(Dispatchers.Main) {
                        showErrorToast("Network error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateStreakDisplay(days: Int) {
        val streakDisplay = findViewById<TextView>(R.id.streakCountText)
        
        if (days > previousStreak && previousStreak > 0) {
            // Streak increased - show animation and dialog
            val animation = AnimationUtils.loadAnimation(this, R.anim.streak_increment)
            streakDisplay.startAnimation(animation)
            showStreakUpdateDialog(days)
        }
        
        streakDisplay.text = days.toString()
        previousStreak = days

        streakDisplay.setOnClickListener {
            showStreakDetailsDialog(days)
        }
    }

    private fun showStreakUpdateDialog(days: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.streak_update_dialog, null)
        
        val dialog = AlertDialog.Builder(this, R.style.StreakDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Customize dialog content
        dialogView.apply {
            findViewById<TextView>(R.id.streakMessage).text = "Streak Increased!"
            findViewById<TextView>(R.id.streakSubMessage).text = 
                "You're on fire! $days days of continuous learning"
        }

        // Add animation to dialog
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        
        dialog.show()

        // Auto dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }

    private fun showStreakDetailsDialog(days: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Your Learning Streak")
            .setMessage("You've been learning consistently for $days days!\n\n" +
                    "Keep practicing daily to maintain your streak.")
            .setPositiveButton("Keep Going!") { dialog, _ -> dialog.dismiss() }
            .show()
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

    private fun showStreakDetails() {
        val streakCount = streakDisplay.text.toString().toIntOrNull() ?: 0
        AlertDialog.Builder(this)
            .setTitle("Streak Details")
            .setMessage("Your current learning streak: $streakCount days")
            .setPositiveButton("Keep it up!") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}