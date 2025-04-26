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
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val userId = sharedPreferences.getLong("userId", -1)
        val token = sharedPreferences.getString("token", null)

        if (userId != -1L && token != null) {
            fetchUserDetails(userId, token)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }

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
            if (token != null) {
                logout(token)
            } else {
                navigateToLogin()
            }
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

    private fun fetchUserDetails(userId: Long, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getUserById(userId, "Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            welcomeTextView.text = "Welcome, ${user.firstName} ${user.lastName}!"
                        }
                    } else {
                        Toast.makeText(this@HomeActivity, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
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
