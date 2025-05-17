package com.example.pronounceit

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var playButton: ImageView
    private lateinit var logoutButton: ImageView
    private lateinit var musicToggleButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var buttonSound: MediaPlayer
    private lateinit var backgroundMusic: MediaPlayer
    private var isMusicPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI elements
        playButton = findViewById(R.id.playButton)
        logoutButton = findViewById(R.id.logoutButton)
        musicToggleButton = findViewById(R.id.musicToggleButton)

        // Initialize button sound
        buttonSound = MediaPlayer.create(this, R.raw.button_click)

        // Initialize background music
        setupBackgroundMusic()

        // Set up music toggle button
        setupMusicToggleButton()

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

        // Play button with sound
        playButton.setOnClickListener {
            playButtonSound()
            val intent = Intent(this, CategoryActivity::class.java)
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
    }

    private fun setupMusicToggleButton() {
        // Initially show the mute button since music is playing
        musicToggleButton.setImageResource(R.drawable.mutebutton)

        musicToggleButton.setOnClickListener {
            playButtonSound() // Play click sound

            if (isMusicPlaying) {
                // Currently playing, so mute it
                backgroundMusic.pause()
                musicToggleButton.setImageResource(R.drawable.mutebutton)
                isMusicPlaying = false
            } else {
                // Currently muted, so play it
                backgroundMusic.start()
                musicToggleButton.setImageResource(R.drawable.musicbutton)
                isMusicPlaying = true
            }
        }
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
        Toast.makeText(this, "Please use the logout button to exit", Toast.LENGTH_SHORT).show()
    }
}