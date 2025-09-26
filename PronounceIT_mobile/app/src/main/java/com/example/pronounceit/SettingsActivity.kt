package com.example.pronounceit

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var myProgressButton: Button
    private lateinit var editUserProfileButton: Button
    private lateinit var volumeToggleButton: Button
    private lateinit var backgroundMusic: MediaPlayer
    private var isMusicPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize buttons
        myProgressButton = findViewById(R.id.myProgressButton)
        editUserProfileButton = findViewById(R.id.editUserProfileButton)
        volumeToggleButton = findViewById(R.id.volumeToggleButton)

        setupBackgroundMusic()
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        myProgressButton.setOnClickListener {
            val intent = Intent(this, MyProgressActivity::class.java)
            startActivity(intent)
        }

        editUserProfileButton.setOnClickListener {
            val intent = Intent(this, EditUserProfileActivity::class.java)
            startActivity(intent)
        }

        volumeToggleButton.setOnClickListener {
            if (isMusicPlaying) {
                backgroundMusic.pause()
                volumeToggleButton.text = "Music: Off"
                isMusicPlaying = false
            } else {
                backgroundMusic.start()
                volumeToggleButton.text = "Music: On"
                isMusicPlaying = true
            }
        }
    }

    private fun setupBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(this, R.raw.homemusic)
        backgroundMusic.isLooping = true
        backgroundMusic.setVolume(0.5f, 0.5f)
        
        // Check saved music preference
        val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        isMusicPlaying = prefs.getBoolean("musicEnabled", true)
        
        if (isMusicPlaying) {
            backgroundMusic.start()
            volumeToggleButton.text = "Music: On"
        } else {
            volumeToggleButton.text = "Music: Off"
        }
    }

    override fun onPause() {
        super.onPause()
        if (::backgroundMusic.isInitialized && backgroundMusic.isPlaying) {
            backgroundMusic.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::backgroundMusic.isInitialized && !backgroundMusic.isPlaying && isMusicPlaying) {
            backgroundMusic.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::backgroundMusic.isInitialized) {
            backgroundMusic.release()
        }
        
        // Save music preference
        getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
            .edit()
            .putBoolean("musicEnabled", isMusicPlaying)
            .apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
