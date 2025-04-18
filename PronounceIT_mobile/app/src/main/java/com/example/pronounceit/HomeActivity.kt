package com.example.pronounceit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var playButton: Button
    private lateinit var settingsButton: Button
    private lateinit var logoutButton: Button
    private lateinit var welcomeTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI elements
        playButton = findViewById(R.id.playButton)
        settingsButton = findViewById(R.id.settingsButton)
        logoutButton = findViewById(R.id.logoutButton)
        welcomeTextView = findViewById(R.id.welcomeTextView)

        // Get shared preferences
        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "Player")

        // Set welcome message with user's name
        welcomeTextView.text = "Welcome, $userName!"

        // Animation for button press
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.button_scale)
        val buttons = listOf(playButton, settingsButton, logoutButton)
        buttons.forEach { btn ->
            btn.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.startAnimation(scaleAnim)
                }
                false
            }
        }

        // Play button
        playButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Settings button
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Logout button
        logoutButton.setOnClickListener {
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Please use the logout button to exit", Toast.LENGTH_SHORT).show()
    }
}
